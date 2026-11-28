package ru.college.app.data.repositoryimpl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.college.app.data.local.db.dao.UnlockedSkillDao
import ru.college.app.data.local.db.entities.UnlockedSkillEntity
import ru.college.app.data.repository.UnlockedSkillRepository

class UnlockedSkillRepositoryImpl(
    private val unlockedSkillDao: UnlockedSkillDao
) : UnlockedSkillRepository {

    override fun observeByUser(userId: Long): Flow<List<UnlockedSkillEntity>> =
        unlockedSkillDao.observeByUser(userId)

    override suspend fun getByUser(userId: Long): List<UnlockedSkillEntity> =
        withContext(Dispatchers.IO) { unlockedSkillDao.getByUser(userId) }

    override suspend fun unlock(skill: UnlockedSkillEntity): Long =
        withContext(Dispatchers.IO) { unlockedSkillDao.insert(skill) }

    override suspend fun deleteForUser(userId: Long) =
        withContext(Dispatchers.IO) { unlockedSkillDao.deleteForUser(userId) }
}
