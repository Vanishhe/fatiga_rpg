package ru.college.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.college.app.data.local.db.entities.PlayerProgressEntity

@Dao
interface PlayerProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: PlayerProgressEntity): Long

    @Query("SELECT * FROM player_progress WHERE userId = :userId LIMIT 1")
    suspend fun findByUser(userId: Long): PlayerProgressEntity?

    @Query("SELECT * FROM player_progress WHERE userId = :userId LIMIT 1")
    fun observeByUser(userId: Long): Flow<PlayerProgressEntity?>

    @Query("DELETE FROM player_progress WHERE userId = :userId")
    suspend fun deleteForUser(userId: Long)
}
