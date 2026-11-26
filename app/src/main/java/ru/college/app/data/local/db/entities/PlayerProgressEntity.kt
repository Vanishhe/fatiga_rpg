package ru.college.app.data.local.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "player_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // One active hero per profile.
    indices = [Index(value = ["userId"], unique = true)]
)
data class PlayerProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    // "WARRIOR" or "ARCHER".
    val heroClass: String,
    val hp: Int,
    val maxHp: Int,
    val attackDamage: Int,
    val defense: Int,
    val gold: Int,
    val offerings: Int,
    val weaponLevel: Int,
    val armorLevel: Int,
    // Up to 8 inventory slots serialised as JSON.
    val inventoryJson: String
)
