package ru.college.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val email: String,
    val name: String,
    // Only the SHA-256 hash is persisted, never the plain password.
    val passwordHash: String,
    val passwordSalt: String,
    val soulLevel: Int = 1,
    val createdAt: Long
)
