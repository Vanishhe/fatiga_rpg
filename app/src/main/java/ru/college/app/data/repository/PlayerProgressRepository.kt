package ru.college.app.data.repository

import kotlinx.coroutines.flow.Flow
import ru.college.app.data.local.db.entities.PlayerProgressEntity

interface PlayerProgressRepository {

    fun observeByUser(userId: Long): Flow<PlayerProgressEntity?>

    suspend fun findByUser(userId: Long): PlayerProgressEntity?

    suspend fun upsert(progress: PlayerProgressEntity): Long

    suspend fun deleteForUser(userId: Long)
}
