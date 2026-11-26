package ru.college.app.di

import android.content.Context
import ru.college.app.data.local.db.AppDatabase
import ru.college.app.data.repository.PlayerProgressRepository
import ru.college.app.data.repository.RunHistoryRepository
import ru.college.app.data.repository.UserRepository
import ru.college.app.data.repositoryimpl.PlayerProgressRepositoryImpl
import ru.college.app.data.repositoryimpl.RunHistoryRepositoryImpl
import ru.college.app.data.repositoryimpl.UserRepositoryImpl

/**
 * Manual dependency injection (no Hilt/Dagger — forbidden by the project spec).
 * Everything is created lazily and lives as long as the Application process.
 */
class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy { AppDatabase.getInstance(context) }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userDao())
    }

    val runHistoryRepository: RunHistoryRepository by lazy {
        RunHistoryRepositoryImpl(database.runHistoryDao())
    }

    val playerProgressRepository: PlayerProgressRepository by lazy {
        PlayerProgressRepositoryImpl(database.playerProgressDao())
    }
}
