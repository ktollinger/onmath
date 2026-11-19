package cz.kt.onmath.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val topic: String,
    val totalProblems: Int,
    val correctFirstTry: Int,
    val timestampEpochMs: Long = System.currentTimeMillis(),
)

@Entity(tableName = "mistake_records")
data class MistakeRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val topic: String,
    val problemKey: String,
    val displayText: String,
    val expectedAnswer: Int,
    val givenAnswer: Int,
    val timestampEpochMs: Long = System.currentTimeMillis(),
)

data class TopicStats(
    val topic: String,
    val sessionCount: Int,
    val totalProblems: Int,
    val totalCorrect: Int,
) {
    val accuracyPercent: Int
        get() = if (totalProblems == 0) 0 else (totalCorrect * 100) / totalProblems
}
