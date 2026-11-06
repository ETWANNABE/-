package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Shape types for the word cloud visual container
 */
enum class CloudShape(val displayName: String, val iconRes: String) {
    CLOUD("구름 ☁️", "ic_cloud"),
    HEART("하트 ❤️", "ic_heart"),
    CIRCLE("원형 🟢", "ic_circle"),
    STAR("별 ⭐", "ic_star"),
    RECTANGLE("직사각형 📊", "ic_rectangle")
}

/**
 * Color palettes for word styling
 */
enum class ColorPalette(val displayName: String, val hexColors: List<Long>) {
    VIBRANT("비브런트 팝", listOf(0xFF6366F1, 0xFFEC4899, 0xFF10B981, 0xFFF59E0B, 0xFF8B5CF6, 0xFF06B6D4)),
    OCEAN("오션 브리즈", listOf(0xFF0284C7, 0xFF06B6D4, 0xFF0EA5E9, 0xFF38BDF8, 0xFF1D4ED8, 0xFF60A5FA)),
    SUNSET("선셋 웜", listOf(0xFFE11D48, 0xFFF97316, 0xFFF59E0B, 0xFFEF4444, 0xFFD97706, 0xFFFB923C)),
    FOREST("포레스트 에머랄드", listOf(0xFF059669, 0xFF10B981, 0xFF34D399, 0xFF047857, 0xFF15803D, 0xFF6EE7B7)),
    NEON_DARK("네온 다크", listOf(0xFF38BDF8, 0xFFF43F5E, 0xFFA855F7, 0xFF34D399, 0xFFFBBF24, 0xFFE879F9)),
    PASTEL("소프트 파스텔", listOf(0xFF818CF8, 0xFFF472B6, 0xFF34D399, 0xFFFBBF24, 0xFFA78BFA, 0xFF38BDF8))
}

@Entity(tableName = "word_cloud_projects")
data class WordCloudProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val shape: String = CloudShape.CLOUD.name,
    val colorPalette: String = ColorPalette.VIBRANT.name,
    val minFontSize: Int = 14,
    val maxFontSize: Int = 54,
    val allowRotation: Boolean = true,
    val excludedWordsJson: String = "[\"은\", \"는\", \"이\", \"가\", \"을\", \"를\", \"에\", \"에서\", \"의\", \"과\", \"와\", \"도\", \"으로\", \"로\"]",
    val includedOnlyWordsJson: String = "[]", // If non-empty, only these words are rendered
    val minFrequencyFilter: Int = 1, // Exclude words with count < minFrequencyFilter
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "word_responses",
    foreignKeys = [
        ForeignKey(
            entity = WordCloudProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class WordResponseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val rawText: String,
    val submitter: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "word_groups",
    foreignKeys = [
        ForeignKey(
            entity = WordCloudProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class WordGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val canonicalWord: String,
    val note: String = ""
)

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "rawWord"],
    foreignKeys = [
        ForeignKey(
            entity = WordGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class GroupMemberEntity(
    val groupId: Long,
    val rawWord: String
)

@Entity(
    tableName = "word_weight_boosts",
    primaryKeys = ["projectId", "word"],
    foreignKeys = [
        ForeignKey(
            entity = WordCloudProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class WordWeightBoostEntity(
    val projectId: Long,
    val word: String,
    val boostFactor: Float = 1.0f // 1.0 = Normal, 1.5 = 150%, 2.0 = 200%, 3.0 = 300%
)
