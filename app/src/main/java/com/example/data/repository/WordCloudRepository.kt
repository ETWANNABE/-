package com.example.data.repository

import com.example.data.domain.SimilarityAnalyzer
import com.example.data.domain.SimilarityPair
import com.example.data.local.WordCloudDao
import com.example.data.local.WordCloudProjectEntity
import com.example.data.local.WordGroupEntity
import com.example.data.local.WordResponseEntity
import com.example.data.local.WordWeightBoostEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray

data class AggregatedWordData(
    val wordCounts: Map<String, Int>,          // Canonical/Merged word -> Total weighted count
    val groupRawMap: Map<String, List<String>>, // Canonical word -> list of constituent raw words
    val weightBoosts: Map<String, Float>,      // Word -> Boost multiplier (e.g. 1.5x, 2.0x)
    val rawResponseCount: Int,
    val excludedWords: Set<String>,
    val includedOnlyWords: Set<String>
)

class WordCloudRepository(private val dao: WordCloudDao) {

    fun getAllProjects(): Flow<List<WordCloudProjectEntity>> = dao.getAllProjectsFlow()

    fun getProjectById(projectId: Long): Flow<WordCloudProjectEntity?> = dao.getProjectByIdFlow(projectId)

    suspend fun createProject(
        title: String,
        description: String = "",
        shape: String,
        colorPalette: String
    ): Long {
        val project = WordCloudProjectEntity(
            title = title,
            description = description,
            shape = shape,
            colorPalette = colorPalette
        )
        return dao.insertProject(project)
    }

    suspend fun updateProject(project: WordCloudProjectEntity) {
        dao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(project: WordCloudProjectEntity) {
        dao.deleteProject(project)
    }

    suspend fun addSingleResponse(projectId: Long, rawText: String, submitter: String = ""): Long {
        val clean = rawText.trim()
        if (clean.isEmpty()) return -1L
        return dao.insertResponse(
            WordResponseEntity(
                projectId = projectId,
                rawText = clean,
                submitter = submitter.ifBlank { "익명" }
            )
        )
    }

    suspend fun addBatchTextResponses(projectId: Long, textBlock: String, submitter: String = "") {
        val delimiters = Regex("[\n,;.]+")
        val rawList = textBlock.split(delimiters)
            .flatMap { it.trim().split(Regex("\\s+")) }
            .map { it.trim().replace(Regex("[^a-zA-Z0-9가-힣]"), "") }
            .filter { it.length >= 1 }

        if (rawList.isEmpty()) return

        val entities = rawList.map { word ->
            WordResponseEntity(
                projectId = projectId,
                rawText = word,
                submitter = submitter.ifBlank { "일괄 입력" }
            )
        }
        dao.insertResponses(entities)
    }

    fun getAggregatedWordDataFlow(projectId: Long): Flow<AggregatedWordData> {
        val responsesFlow = dao.getResponsesForProjectFlow(projectId)
        val groupsFlow = dao.getGroupsForProjectFlow(projectId)
        val membersFlow = dao.getAllGroupMembersForProjectFlow(projectId)
        val boostsFlow = dao.getWeightBoostsForProjectFlow(projectId)
        val projectFlow = dao.getProjectByIdFlow(projectId)

        return combine(responsesFlow, groupsFlow, membersFlow, boostsFlow, projectFlow) { responses, groups, members, boosts, project ->
            val excludedWords = parseJsonArraySet(project?.excludedWordsJson)
            val includedOnlyWords = parseJsonArraySet(project?.includedOnlyWordsJson)
            val minFreqFilter = project?.minFrequencyFilter ?: 1

            val boostMap = boosts.associate { it.word to it.boostFactor }

            // Map: rawWord -> WordGroup
            val rawToGroupMap = mutableMapOf<String, WordGroupEntity>()
            members.forEach { member ->
                val group = groups.find { it.id == member.groupId }
                if (group != null) {
                    rawToGroupMap[member.rawWord] = group
                }
            }

            val rawCounts = mutableMapOf<String, Int>()
            val groupRawMap = mutableMapOf<String, MutableSet<String>>()

            responses.forEach { resp ->
                val rawWord = resp.rawText.trim()
                if (rawWord.isEmpty()) return@forEach

                val lower = rawWord.lowercase()

                // Filtering rules
                if (excludedWords.contains(lower)) return@forEach
                if (includedOnlyWords.isNotEmpty() && !includedOnlyWords.contains(lower)) return@forEach

                val group = rawToGroupMap[rawWord]
                val canonicalKey = group?.canonicalWord ?: rawWord

                rawCounts[canonicalKey] = (rawCounts[canonicalKey] ?: 0) + 1

                val rawSet = groupRawMap.getOrPut(canonicalKey) { mutableSetOf() }
                rawSet.add(rawWord)
            }

            // Apply frequency filter & weight boosts
            val finalCounts = mutableMapOf<String, Int>()
            rawCounts.forEach { (word, baseCount) ->
                if (baseCount >= minFreqFilter) {
                    val boost = boostMap[word] ?: 1.0f
                    val weightedCount = kotlin.math.round(baseCount * boost).toInt().coerceAtLeast(1)
                    finalCounts[word] = weightedCount
                }
            }

            AggregatedWordData(
                wordCounts = finalCounts,
                groupRawMap = groupRawMap.mapValues { it.value.toList() },
                weightBoosts = boostMap,
                rawResponseCount = responses.size,
                excludedWords = excludedWords,
                includedOnlyWords = includedOnlyWords
            )
        }
    }

    suspend fun getSimilarSuggestions(projectId: Long): List<SimilarityPair> {
        val responses = dao.getResponsesForProject(projectId)

        val rawCounts = mutableMapOf<String, Int>()
        responses.forEach { resp ->
            val word = resp.rawText.trim()
            if (word.isNotEmpty()) {
                rawCounts[word] = (rawCounts[word] ?: 0) + 1
            }
        }

        return SimilarityAnalyzer.findSimilarPairs(rawCounts)
    }

    suspend fun setWordWeightBoost(projectId: Long, word: String, boostFactor: Float) {
        if (boostFactor <= 1.0f) {
            dao.deleteWeightBoost(projectId, word)
        } else {
            dao.setWeightBoost(WordWeightBoostEntity(projectId = projectId, word = word, boostFactor = boostFactor))
        }
    }

    suspend fun mergeSimilarWords(projectId: Long, canonicalWord: String, memberWords: List<String>) {
        if (memberWords.isEmpty()) return
        dao.createWordGroup(projectId, canonicalWord, memberWords)
    }

    suspend fun unmergeGroup(groupId: Long) {
        dao.deleteGroup(groupId)
    }

    suspend fun deleteResponseWord(projectId: Long, word: String) {
        dao.deleteResponsesByWord(projectId, word)
    }

    suspend fun clearAllResponses(projectId: Long) {
        dao.clearAllResponses(projectId)
    }

    private fun parseJsonArraySet(json: String?): Set<String> {
        if (json.isNullOrBlank()) return emptySet()
        return try {
            val array = JSONArray(json)
            val result = mutableSetOf<String>()
            for (i in 0 until array.length()) {
                result.add(array.getString(i).trim().lowercase())
            }
            result
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun toJsonArrayString(set: Set<String>): String {
        val array = JSONArray()
        set.forEach { array.put(it) }
        return array.toString()
    }
}
