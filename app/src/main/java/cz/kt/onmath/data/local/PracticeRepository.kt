package cz.kt.onmath.data.local

import cz.kt.onmath.domain.model.MathTopic
import kotlinx.coroutines.flow.Flow

interface PracticeRepository {
    fun getRecentSessions(limit: Int = 20): Flow<List<PracticeSessionEntity>>
    fun getTopicStats(): Flow<List<TopicStats>>
    fun getRecentMistakes(limit: Int = 30): Flow<List<MistakeRecordEntity>>

    suspend fun recordSession(topic: MathTopic, totalProblems: Int, correctFirstTry: Int)
    suspend fun recordMistake(
        topic: MathTopic,
        problemKey: String,
        displayText: String,
        expectedAnswer: Int,
        givenAnswer: Int,
    )
    suspend fun clearAllHistory()
}

class OfflinePracticeRepository(
    private val practiceDao: PracticeDao,
) : PracticeRepository {

    override fun getRecentSessions(limit: Int): Flow<List<PracticeSessionEntity>> =
        practiceDao.getRecentSessions(limit)

    override fun getTopicStats(): Flow<List<TopicStats>> =
        practiceDao.getTopicStats()

    override fun getRecentMistakes(limit: Int): Flow<List<MistakeRecordEntity>> =
        practiceDao.getRecentMistakes(limit)

    override suspend fun recordSession(topic: MathTopic, totalProblems: Int, correctFirstTry: Int) {
        practiceDao.insertSession(
            PracticeSessionEntity(
                topic = topic.name,
                totalProblems = totalProblems,
                correctFirstTry = correctFirstTry,
            )
        )
    }

    override suspend fun recordMistake(
        topic: MathTopic,
        problemKey: String,
        displayText: String,
        expectedAnswer: Int,
        givenAnswer: Int,
    ) {
        practiceDao.insertMistake(
            MistakeRecordEntity(
                topic = topic.name,
                problemKey = problemKey,
                displayText = displayText,
                expectedAnswer = expectedAnswer,
                givenAnswer = givenAnswer,
            )
        )
    }

    override suspend fun clearAllHistory() {
        practiceDao.clearSessions()
        practiceDao.clearMistakes()
    }
}
