package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WordCloudProjectEntity::class,
        WordResponseEntity::class,
        WordGroupEntity::class,
        GroupMemberEntity::class,
        WordWeightBoostEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WordCloudDatabase : RoomDatabase() {

    abstract fun wordCloudDao(): WordCloudDao

    companion object {
        @Volatile
        private var INSTANCE: WordCloudDatabase? = null

        fun getInstance(context: Context): WordCloudDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordCloudDatabase::class.java,
                    "word_cloud_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getInstance(context).wordCloudDao()
                    seedInitialTemplates(dao)
                }
            }

            private suspend fun seedInitialTemplates(dao: WordCloudDao) {
                // Template 1: Mood Survey
                val p1Id = dao.insertProject(
                    WordCloudProjectEntity(
                        title = "오늘의 기분 및 감정 조사 🌈",
                        description = "팀원들과 주고받은 오늘 하루 나의 감정 및 상태 표현",
                        shape = CloudShape.CLOUD.name,
                        colorPalette = ColorPalette.VIBRANT.name
                    )
                )
                val moodResponses = listOf(
                    "행복함", "행복함", "행복해요", "행복해요", "행복함", "행복",
                    "피곤함", "피곤해요", "피곤함", "피곤해요", "피곤",
                    "설렘", "설레요", "설렘", "설렘",
                    "뿌듯함", "뿌듯해요", "뿌듯함",
                    "기쁨", "기뻐요", "기쁨",
                    "기대돼요", "기대됨",
                    "편안함", "편안해요", "평화로움",
                    "우울함", "스트레스", "지침", "열정적", "감사함"
                )
                moodResponses.forEach { word ->
                    dao.insertResponse(WordResponseEntity(projectId = p1Id, rawText = word, submitter = "익명"))
                }
                dao.createWordGroup(p1Id, "행복함 (행복해요)", listOf("행복함", "행복해요", "행복"))
                dao.setWeightBoost(WordWeightBoostEntity(projectId = p1Id, word = "행복함 (행복해요)", boostFactor = 1.5f))

                // Template 2: Product Ideas
                val p2Id = dao.insertProject(
                    WordCloudProjectEntity(
                        title = "우리 팀 신제품 아이디어 💡",
                        description = "2026 하반기 신규 서비스 및 제품 핵심 키워드",
                        shape = CloudShape.STAR.name,
                        colorPalette = ColorPalette.NEON_DARK.name
                    )
                )
                val ideaResponses = listOf(
                    "친환경", "친환경", "친환경", "친환경적인",
                    "AI기능", "AI기능", "AI자동화", "인공지능",
                    "손쉬운사용", "쉬운사용", "사용성", "직관적",
                    "보조배터리", "가성비", "가성비", "디자인",
                    "가벼움", "가벼워요", "휴대성", "연동성",
                    "안전성", "고속충전", "스마트", "내구성"
                )
                ideaResponses.forEach { word ->
                    dao.insertResponse(WordResponseEntity(projectId = p2Id, rawText = word, submitter = "아이디어위원"))
                }

                // Template 3: Dinner Menu
                val p3Id = dao.insertProject(
                    WordCloudProjectEntity(
                        title = "금요일 회식 추천 메뉴 🍕",
                        description = "부서 회식 시 먹고 싶은 음식 투표",
                        shape = CloudShape.HEART.name,
                        colorPalette = ColorPalette.SUNSET.name
                    )
                )
                val menuResponses = listOf(
                    "삼겹살", "삼겹살", "삼겹살", "삼겹살", "삼겹살",
                    "치킨", "치킨", "치킨", "양념치킨",
                    "마라탕", "마라탕", "마라샹궈",
                    "초밥", "초밥", "스시",
                    "파스타", "파스타", "떡볶이", "족발", "소고기", "회"
                )
                menuResponses.forEach { word ->
                    dao.insertResponse(WordResponseEntity(projectId = p3Id, rawText = word, submitter = "팀원"))
                }

                // Template 4: Workshop Feedback
                val p4Id = dao.insertProject(
                    WordCloudProjectEntity(
                        title = "워크숍 수강 후기 키워드 📚",
                        description = "이번 세미나 참석자 만족도 및 의견 한줄평",
                        shape = CloudShape.CIRCLE.name,
                        colorPalette = ColorPalette.OCEAN.name
                    )
                )
                val feedbackResponses = listOf(
                    "유익해요", "유익함", "유익함", "유익해요", "유익했습니다",
                    "재밌어요", "재미있음", "재밌음",
                    "실용적", "실용적임", "실용적이에요",
                    "강추", "강추합니다", "알참", "친절함", "명쾌함", "대족"
                )
                feedbackResponses.forEach { word ->
                    dao.insertResponse(WordResponseEntity(projectId = p4Id, rawText = word, submitter = "수강생"))
                }
            }
        }
    }
}
