package ru.college.app.data.repository

import kotlinx.coroutines.flow.Flow
import ru.college.app.data.local.db.entities.UserEntity

interface UserRepository {

    fun observeUsers(): Flow<List<UserEntity>>

    suspend fun findUserByEmail(email: String): UserEntity?

    suspend fun findUserById(id: Long): UserEntity?

    fun observeUser(userId: Long): Flow<UserEntity?>

    suspend fun insertUser(user: UserEntity): Long

    suspend fun updateProfile(userId: Long, name: String, soulLevel: Int)

    suspend fun deleteUser(userId: Long)

    suspend fun isEmailTaken(email: String): Boolean
}
