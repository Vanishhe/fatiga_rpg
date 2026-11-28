package ru.college.app.domain.model

/**
 * Enemy template. For the MVP the portal always spawns goblins; deeper floors
 * scale the same template via CombatEngine.
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
 * Item template shared by the Armory (prices) and the Battle inventory (heal).
 */
data class ItemDefinition(
    val id: String,
    val heal: Int,
    val price: Int
)

object EnemyCatalog {
    val GOBLIN = EnemyDefinition(
        id = "GOBLIN",
        maxHp = 24,
        attack = 6,
        defense = 1,
        gold = 15,
        offerings = 1
    )
}

object ItemCatalog {
    const val RED = "POTION_RED"
    const val BLUE = "POTION_BLUE"

    val red = ItemDefinition(id = RED, heal = 12, price = 20)
    val blue = ItemDefinition(id = BLUE, heal = 24, price = 40)

    val all = listOf(red, blue)

    fun byId(id: String): ItemDefinition? = all.firstOrNull { it.id == id }
}
