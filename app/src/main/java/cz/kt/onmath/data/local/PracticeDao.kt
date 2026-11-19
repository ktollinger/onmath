package cz.kt.onmath.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {

    @Insert
    suspend fun insertSession(session: PracticeSessionEntity)

    @Insert
    suspend fun insertMistake(mistake: MistakeRecordEntity)

    @Query("SELECT * FROM practice_sessions ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 20): Flow<List<PracticeSessionEntity>>

    @Query(
        """
        SELECT topic,
               COUNT(*) AS sessionCount,
               SUM(totalProblems) AS totalProblems,
               SUM(correctFirstTry) AS totalCorrect
        FROM practice_sessions
        GROUP BY topic
        """
    )
    fun getTopicStats(): Flow<List<TopicStats>>

    @Query("SELECT * FROM mistake_records ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun getRecentMistakes(limit: Int = 30): Flow<List<MistakeRecordEntity>>

    @Query("DELETE FROM practice_sessions")
    suspend fun clearSessions()

    @Query("DELETE FROM mistake_records")
    suspend fun clearMistakes()
}
