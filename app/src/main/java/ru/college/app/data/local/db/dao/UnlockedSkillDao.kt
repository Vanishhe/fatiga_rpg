package ru.college.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.college.app.data.local.db.entities.UnlockedSkillEntity

@Dao
interface UnlockedSkillDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(skill: UnlockedSkillEntity): Long

    @Query("SELECT * FROM unlocked_skills WHERE userId = :userId ORDER BY unlockedAt ASC")
    fun observeByUser(userId: Long): Flow<List<UnlockedSkillEntity>>

    @Query("SELECT * FROM unlocked_skills WHERE userId = :userId")
    suspend fun getByUser(userId: Long): List<UnlockedSkillEntity>

    @Query("DELETE FROM unlocked_skills WHERE userId = :userId")
    suspend fun deleteForUser(userId: Long)
}
