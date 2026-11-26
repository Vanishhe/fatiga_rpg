package ru.college.app.domain.engine

import kotlin.random.Random

/**
 * Result of one attack roll. Kept as a value object so the ViewModel
 * can decide how to render it without knowing combat math.
 */
data class DamageRoll(
    val damage: Int,
    val hit: Boolean,
    val critical: Boolean = false
)

/**
 * Pure combat rules. Every function is deterministic given [random],
 * which makes the engine unit-testable without Android.
 */
object CombatEngine {

    /** Basic attack: always hits, small variance, never below 1. */
    fun basicDamage(attack: Int, defense: Int, random: Random = Random.Default): Int {
        val base = (attack - defense / 2).coerceAtLeast(1)
        val variance = random.nextInt(-1, 2)
        return (base + variance).coerceAtLeast(1)
    }

    /** Warrior SPECIAL: heavy blow, 25% chance to go wide. */
    fun warriorSpecial(attack: Int, defense: Int, random: Random = Random.Default): DamageRoll {
        if (random.nextFloat() < 0.25f) return DamageRoll(damage = 0, hit = false)
        val base = (attack * 2 - defense / 2).coerceAtLeast(1)
        val damage = (base + random.nextInt(-1, 2)).coerceAtLeast(1)
        return DamageRoll(damage = damage, hit = true)
    }

    /** Archer SPECIAL: two arrows, each can miss, each can crit for 1.5x. */
    fun archerSpecial(attack: Int, defense: Int, random: Random = Random.Default): DamageRoll {
        var total = 0
        var critical = false
        repeat(2) {
            if (random.nextFloat() < 0.8f) {
                var damage = (attack * 3 / 4 - defense / 2).coerceAtLeast(1)
                if (random.nextFloat() < 0.25f) {
                    damage = damage * 3 / 2
                    critical = true
                }
                total += damage
            }
        }
        return DamageRoll(damage = total, hit = total > 0, critical = critical)
    }

    /** Escape attempt: the run ends with half the gold on success. */
    fun fleeSucceeds(random: Random = Random.Default): Boolean = random.nextFloat() < 0.5f

    // Deeper floors are tougher. Scaling lives here so UI never does math.
    fun scaledHp(base: Int, floor: Int): Int = base + (floor - 1) * 6

    fun scaledAttack(base: Int, floor: Int): Int = base + (floor - 1) * 2

    fun scaledDefense(base: Int, floor: Int): Int = base + (floor - 1)
}
