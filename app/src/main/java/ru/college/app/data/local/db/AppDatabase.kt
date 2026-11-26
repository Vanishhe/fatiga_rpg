package ru.college.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.college.app.data.local.db.dao.PlayerProgressDao
import ru.college.app.data.local.db.dao.RunHistoryDao
import ru.college.app.data.local.db.dao.UserDao
import ru.college.app.data.local.db.entities.PlayerProgressEntity
import ru.college.app.data.local.db.entities.RunHistoryEntity
import ru.college.app.data.local.db.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        RunHistoryEntity::class,
        PlayerProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun runHistoryDao(): RunHistoryDao
    abstract fun playerProgressDao(): PlayerProgressDao

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
                ).build().also { instance = it }
            }
    }
}
