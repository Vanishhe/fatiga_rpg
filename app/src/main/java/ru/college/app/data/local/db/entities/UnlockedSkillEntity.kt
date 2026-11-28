package ru.college.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per skill unlocked by a profile. The composite unique index makes
 * unlocking idempotent at the database level, not just in the ViewModel.
 */
@Entity(
    tableName = "unlocked_skills",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "skillId"], unique = true)
    ]
)
data class UnlockedSkillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val skillId: String,
    val unlockedAt: Long
)
