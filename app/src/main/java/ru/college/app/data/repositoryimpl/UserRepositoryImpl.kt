package ru.college.app.data.repositoryimpl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.college.app.data.local.db.dao.UserDao
import ru.college.app.data.local.db.entities.UserEntity
import ru.college.app.data.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override fun observeUsers(): Flow<List<UserEntity>> = userDao.observeAll()

    // Room queries are shifted to Dispatchers.IO so the main thread never blocks.
    override suspend fun findUserByEmail(email: String): UserEntity? =
        withContext(Dispatchers.IO) { userDao.findByEmail(email) }

    override suspend fun findUserById(id: Long): UserEntity? =
        withContext(Dispatchers.IO) { userDao.findById(id) }

    override fun observeUser(userId: Long): Flow<UserEntity?> = userDao.observeById(userId)

    override suspend fun insertUser(user: UserEntity): Long =
        withContext(Dispatchers.IO) { userDao.insert(user) }

    override suspend fun updateProfile(userId: Long, name: String, soulLevel: Int) =
        withContext(Dispatchers.IO) { userDao.updateProfile(userId, name, soulLevel) }

    override suspend fun deleteUser(userId: Long) =
        withContext(Dispatchers.IO) { userDao.deleteById(userId) }

    override suspend fun isEmailTaken(email: String): Boolean =
        withContext(Dispatchers.IO) { userDao.countByEmail(email) > 0 }
}
