package ru.college.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.college.app.data.local.db.dao.PlayerProgressDao
import ru.college.app.data.local.db.dao.RunHistoryDao
import ru.college.app.data.local.db.dao.UnlockedSkillDao
import ru.college.app.data.local.db.dao.UserDao
import ru.college.app.data.local.db.entities.PlayerProgressEntity
import ru.college.app.data.local.db.entities.RunHistoryEntity
import ru.college.app.data.local.db.entities.UnlockedSkillEntity
import ru.college.app.data.local.db.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        RunHistoryEntity::class,
        PlayerProgressEntity::class,
        UnlockedSkillEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun runHistoryDao(): RunHistoryDao
    abstract fun playerProgressDao(): PlayerProgressDao
    abstract fun unlockedSkillDao(): UnlockedSkillDao

    companion object {
        private const val DATABASE_NAME = "fatiga.db"

        @Volatile
        private var instance: AppDatabase? = null

        // Double-checked locking keeps a single Room instance per process.
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // The schema changed from v1 to v2 (skill tree). The project
                    // is a local portfolio build, so a destructive fallback is
                    // acceptable and explicitly required by the task.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
