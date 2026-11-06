package com.example.data.domain

import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class SimilarityPair(
    val wordA: String,
    val countA: Int,
    val wordB: String,
    val countB: Int,
    val similarityScore: Float, // 0.0 to 1.0
    val suggestedCanonical: String
)

object SimilarityAnalyzer {

    private val SUFFIXES = listOf(
        "입니다", "이에요", "예요", "했습니다", "해요", "함", "음", "다", "적임", "적", "성",
        "하는", "스러운", "스럽다", "적인"
    )

    private val PARTICLES = listOf(
        "은", "는", "이", "가", "을", "를", "에", "에서", "의", "과", "와", "도", "으로", "로"
    )

    /**
     * Normalizes a Korean word or short expression by removing punctuation, spaces,
     * and stripping common verb/adjective endings or particle suffixes.
     */
    fun normalizeStem(word: String): String {
        var clean = word.trim().lowercase(Locale.KOREAN)
            .replace(Regex("[^a-zA-Z0-9가-힣]"), "")

        if (clean.length <= 1) return clean

        // Strip particles first if word length > 2
        for (particle in PARTICLES) {
            if (clean.length > 2 && clean.endsWith(particle)) {
                clean = clean.substring(0, clean.length - particle.length)
                break
            }
        }

        // Strip verb/noun suffixes
        for (suffix in SUFFIXES) {
            if (clean.length > 2 && clean.endsWith(suffix)) {
                clean = clean.substring(0, clean.length - suffix.length)
                break
            }
        }

        return clean
    }

    /**
     * Calculates string similarity between 0.0 and 1.0 using Levenshtein distance
     * combined with stem normalization matching.
     */
    fun calculateSimilarity(word1: String, word2: String): Float {
        val w1 = word1.trim().lowercase()
        val w2 = word2.trim().lowercase()

        if (w1 == w2) return 1.0f

        val stem1 = normalizeStem(w1)
        val stem2 = normalizeStem(w2)

        // Exact stem match (e.g., "행복함" and "행복해요" both normalize to "행복")
        if (stem1.isNotEmpty() && stem1 == stem2) {
            return 0.95f
        }

        // Substring containment for Korean words longer than 2 characters
        if (stem1.length >= 2 && stem2.length >= 2) {
            if (stem1.contains(stem2) || stem2.contains(stem1)) {
                val lenMin = min(stem1.length, stem2.length).toFloat()
                val lenMax = max(stem1.length, stem2.length).toFloat()
                return 0.80f + 0.15f * (lenMin / lenMax)
            }
        }

        // Levenshtein edit distance
        val dist = levenshteinDistance(w1, w2)
        val maxLen = max(w1.length, w2.length)
        if (maxLen == 0) return 1.0f

        val editSim = 1.0f - (dist.toFloat() / maxLen.toFloat())
        return editSim
    }

    /**
     * Analyzes a map of (Word -> Frequency) and identifies pairs of words that are similar
     * above a confidence threshold (e.g. >= 0.70).
     */
    fun findSimilarPairs(
        wordCounts: Map<String, Int>,
        threshold: Float = 0.70f
    ): List<SimilarityPair> {
        val wordList = wordCounts.keys.toList()
        val pairs = mutableListOf<SimilarityPair>()

        for (i in 0 until wordList.size) {
            val wordA = wordList[i]
            val countA = wordCounts[wordA] ?: 1

            for (j in i + 1 until wordList.size) {
                val wordB = wordList[j]
                val countB = wordCounts[wordB] ?: 1

                val sim = calculateSimilarity(wordA, wordB)
                if (sim >= threshold) {
                    val stemA = normalizeStem(wordA)
                    val stemB = normalizeStem(wordB)
                    val suggested = when {
                        countA >= countB -> wordA
                        countB > countA -> wordB
                        stemA.length <= stemB.length -> stemA
                        else -> stemB
                    }

                    pairs.add(
                        SimilarityPair(
                            wordA = wordA,
                            countA = countA,
                            wordB = wordB,
                            countB = countB,
                            similarityScore = sim,
                            suggestedCanonical = suggested
                        )
                    )
                }
            }
        }

        return pairs.sortedByDescending { it.similarityScore }
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[m][n]
    }
}
