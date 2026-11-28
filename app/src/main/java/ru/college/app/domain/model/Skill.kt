package ru.college.app.domain.model

/**
 * Pure skill-tree definition. Stats are applied to PlayerProgressEntity once,
 * at unlock time, so combat never has to re-calculate permanent bonuses.
 */
data class SkillDefinition(
    val id: String,
    val heroClass: HeroClass,
    val tier: Int,
    val cost: Int,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val maxHpBonus: Int = 0
)

object SkillCatalog {

    val skills = listOf(
        // Warrior branch.
        SkillDefinition(
            id = "IRON_GUARD",
            heroClass = HeroClass.WARRIOR,
            tier = 1,
            cost = 1,
            defenseBonus = 1
        ),
        SkillDefinition(
            id = "CRUSHING_BLOW",
            heroClass = HeroClass.WARRIOR,
            tier = 2,
            cost = 1,
            attackBonus = 2
        ),
        SkillDefinition(
            id = "BLOOD_OATH",
            heroClass = HeroClass.WARRIOR,
            tier = 3,
            cost = 2,
            maxHpBonus = 8
        ),

        // Archer branch.
        SkillDefinition(
            id = "STEADY_AIM",
            heroClass = HeroClass.ARCHER,
            tier = 1,
            cost = 1,
            attackBonus = 1
        ),
        SkillDefinition(
            id = "TWIN_SHOT",
            heroClass = HeroClass.ARCHER,
            tier = 2,
            cost = 1,
            attackBonus = 2
        ),
        SkillDefinition(
            id = "SHADOW_STEP",
            heroClass = HeroClass.ARCHER,
            tier = 3,
            cost = 2,
            defenseBonus = 1,
            maxHpBonus = 4
        )
    )

    fun byId(id: String): SkillDefinition? = skills.firstOrNull { it.id == id }

    fun forClass(heroClass: HeroClass): List<SkillDefinition> =
        skills.filter { it.heroClass == heroClass }.sortedBy { it.tier }

    /** A tier is available only when every lower tier of the same branch is owned. */
    fun prerequisitesMet(skill: SkillDefinition, unlockedIds: Set<String>): Boolean =
        forClass(skill.heroClass)
            .filter { it.tier < skill.tier }
            .all { it.id in unlockedIds }
}
