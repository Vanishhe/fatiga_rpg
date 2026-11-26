package ru.college.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.college.app.data.local.db.entities.RunHistoryEntity

@Dao
interface RunHistoryDao {

    @Insert
    suspend fun insert(run: RunHistoryEntity): Long

    @Query("SELECT * FROM run_history WHERE userId = :userId ORDER BY endedAt DESC")
    fun observeByUser(userId: Long): Flow<List<RunHistoryEntity>>

    @Query("SELECT * FROM run_history ORDER BY endedAt DESC")
    fun observeAll(): Flow<List<RunHistoryEntity>>

    @Query("DELETE FROM run_history WHERE userId = :userId")
    suspend fun deleteForUser(userId: Long)
}
