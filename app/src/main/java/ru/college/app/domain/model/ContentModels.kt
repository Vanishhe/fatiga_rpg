package ru.college.app.domain.model

/**
 * Enemy template loaded from assets/enemies.json.
 * Pure data — no Android or Compose imports.
 */
data class EnemyDefinition(
    val id: String,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val gold: Int,
    val offerings: Int
)

/**
 * Item template loaded from assets/items.json.
 */
data class ItemDefinition(
    val id: String,
    val heal: Int,
    val price: Int
)
