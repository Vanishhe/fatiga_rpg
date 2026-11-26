package ru.college.app.data.repository

import kotlinx.coroutines.flow.Flow
import ru.college.app.data.local.db.entities.RunHistoryEntity

interface RunHistoryRepository {

    fun observeByUser(userId: Long): Flow<List<RunHistoryEntity>>

    suspend fun insert(run: RunHistoryEntity): Long

    suspend fun deleteForUser(userId: Long)
}
