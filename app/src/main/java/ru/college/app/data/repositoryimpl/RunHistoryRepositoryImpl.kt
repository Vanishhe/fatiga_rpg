package ru.college.app.data.repositoryimpl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.college.app.data.local.db.dao.RunHistoryDao
import ru.college.app.data.local.db.entities.RunHistoryEntity
import ru.college.app.data.repository.RunHistoryRepository

class RunHistoryRepositoryImpl(
    private val runHistoryDao: RunHistoryDao
) : RunHistoryRepository {

    override fun observeByUser(userId: Long): Flow<List<RunHistoryEntity>> =
        runHistoryDao.observeByUser(userId)

    override suspend fun insert(run: RunHistoryEntity): Long =
        withContext(Dispatchers.IO) { runHistoryDao.insert(run) }

    override suspend fun deleteForUser(userId: Long) =
        withContext(Dispatchers.IO) { runHistoryDao.deleteForUser(userId) }
}
