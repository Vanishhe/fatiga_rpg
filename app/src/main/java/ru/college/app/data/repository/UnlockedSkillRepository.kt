package ru.college.app.data.repository

import kotlinx.coroutines.flow.Flow
import ru.college.app.data.local.db.entities.UnlockedSkillEntity

interface UnlockedSkillRepository {

    fun observeByUser(userId: Long): Flow<List<UnlockedSkillEntity>>

    suspend fun getByUser(userId: Long): List<UnlockedSkillEntity>

    suspend fun unlock(skill: UnlockedSkillEntity): Long

    suspend fun deleteForUser(userId: Long)
}
