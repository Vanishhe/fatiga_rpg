package ru.college.app.data.repositoryimpl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.college.app.data.local.db.dao.PlayerProgressDao
import ru.college.app.data.local.db.entities.PlayerProgressEntity
import ru.college.app.data.repository.PlayerProgressRepository

class PlayerProgressRepositoryImpl(
    private val playerProgressDao: PlayerProgressDao
) : PlayerProgressRepository {

    override fun observeByUser(userId: Long): Flow<PlayerProgressEntity?> =
        playerProgressDao.observeByUser(userId)

    override suspend fun findByUser(userId: Long): PlayerProgressEntity? =
        withContext(Dispatchers.IO) { playerProgressDao.findByUser(userId) }

    override suspend fun upsert(progress: PlayerProgressEntity): Long =
        withContext(Dispatchers.IO) { playerProgressDao.upsert(progress) }

    override suspend fun deleteForUser(userId: Long) =
        withContext(Dispatchers.IO) { playerProgressDao.deleteForUser(userId) }
}
