package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCloudDao {
    // Projects
    @Query("SELECT * FROM word_cloud_projects ORDER BY updatedAt DESC")
    fun getAllProjectsFlow(): Flow<List<WordCloudProjectEntity>>

    @Query("SELECT * FROM word_cloud_projects WHERE id = :projectId")
    fun getProjectByIdFlow(projectId: Long): Flow<WordCloudProjectEntity?>

    @Query("SELECT * FROM word_cloud_projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: Long): WordCloudProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: WordCloudProjectEntity): Long

    @Update
    suspend fun updateProject(project: WordCloudProjectEntity)

    @Delete
    suspend fun deleteProject(project: WordCloudProjectEntity)

    // Responses
    @Query("SELECT * FROM word_responses WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getResponsesForProjectFlow(projectId: Long): Flow<List<WordResponseEntity>>

    @Query("SELECT * FROM word_responses WHERE projectId = :projectId")
    suspend fun getResponsesForProject(projectId: Long): List<WordResponseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: WordResponseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponses(responses: List<WordResponseEntity>)

    @Query("DELETE FROM word_responses WHERE id = :responseId")
    suspend fun deleteResponse(responseId: Long)

    @Query("DELETE FROM word_responses WHERE projectId = :projectId AND rawText = :rawWord")
    suspend fun deleteResponsesByWord(projectId: Long, rawWord: String)

    @Query("DELETE FROM word_responses WHERE projectId = :projectId")
    suspend fun clearAllResponses(projectId: Long)

    // Word Groups
    @Query("SELECT * FROM word_groups WHERE projectId = :projectId")
    fun getGroupsForProjectFlow(projectId: Long): Flow<List<WordGroupEntity>>

    @Query("SELECT * FROM word_groups WHERE projectId = :projectId")
    suspend fun getGroupsForProject(projectId: Long): List<WordGroupEntity>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    suspend fun getGroupMembers(groupId: Long): List<GroupMemberEntity>

    @Query("SELECT * FROM group_members WHERE groupId IN (SELECT id FROM word_groups WHERE projectId = :projectId)")
    fun getAllGroupMembersForProjectFlow(projectId: Long): Flow<List<GroupMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: WordGroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(members: List<GroupMemberEntity>)

    @Query("DELETE FROM word_groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Long)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND rawWord = :rawWord")
    suspend fun removeGroupMember(groupId: Long, rawWord: String)

    @Transaction
    suspend fun createWordGroup(projectId: Long, canonicalWord: String, memberWords: List<String>): Long {
        val groupId = insertGroup(WordGroupEntity(projectId = projectId, canonicalWord = canonicalWord))
        val members = memberWords.map { GroupMemberEntity(groupId = groupId, rawWord = it) }
        insertGroupMembers(members)
        return groupId
    }

    // Weight Boosts
    @Query("SELECT * FROM word_weight_boosts WHERE projectId = :projectId")
    fun getWeightBoostsForProjectFlow(projectId: Long): Flow<List<WordWeightBoostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setWeightBoost(boost: WordWeightBoostEntity)

    @Query("DELETE FROM word_weight_boosts WHERE projectId = :projectId AND word = :word")
    suspend fun deleteWeightBoost(projectId: Long, word: String)
}
