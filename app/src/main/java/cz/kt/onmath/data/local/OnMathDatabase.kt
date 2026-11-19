package cz.kt.onmath.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PracticeSessionEntity::class, MistakeRecordEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class OnMathDatabase : RoomDatabase() {

    abstract fun practiceDao(): PracticeDao

    companion object {
        @Volatile
        private var instance: OnMathDatabase? = null

        fun getDatabase(context: Context): OnMathDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    OnMathDatabase::class.java,
                    "onmath_database",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
