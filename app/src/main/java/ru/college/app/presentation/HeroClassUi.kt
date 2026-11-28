package ru.college.app.presentation

import ru.college.app.domain.model.HeroClass

/**
 * UI-level mirror of HeroClass so Composables never import domain classes.
 */
enum class HeroClassUi(val id: String) {
    WARRIOR(HeroClass.WARRIOR.id),
    ARCHER(HeroClass.ARCHER.id);

    companion object {
        fun fromId(id: String?): HeroClassUi =
            entries.firstOrNull { it.id == id } ?: WARRIOR
    }
}
