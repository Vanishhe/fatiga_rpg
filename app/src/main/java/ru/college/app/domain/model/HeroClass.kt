package ru.college.app.domain.model

/**
 * Pure domain data for the two playable classes. No Android or Compose imports.
 */
enum class HeroClass(
    val id: String,
    val baseHp: Int,
    val baseAttack: Int,
    val baseDefense: Int
) {
    WARRIOR(id = "WARRIOR", baseHp = 48, baseAttack = 8, baseDefense = 6),
    ARCHER(id = "ARCHER", baseHp = 36, baseAttack = 8, baseDefense = 3);

    companion object {
        fun fromId(id: String?): HeroClass =
            entries.firstOrNull { it.id == id } ?: WARRIOR
    }
}
