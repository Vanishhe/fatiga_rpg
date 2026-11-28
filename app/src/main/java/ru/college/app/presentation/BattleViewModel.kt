package ru.college.app.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.college.app.R
import ru.college.app.data.local.db.InventoryCodec
import ru.college.app.data.local.db.InventoryStack
import ru.college.app.data.local.db.entities.PlayerProgressEntity
import ru.college.app.data.local.db.entities.RunHistoryEntity
import ru.college.app.data.repository.PlayerProgressRepository
import ru.college.app.data.repository.RunHistoryRepository
import ru.college.app.data.repository.UserRepository
import ru.college.app.domain.engine.CombatEngine
import ru.college.app.domain.model.EnemyCatalog
import ru.college.app.domain.model.ItemCatalog

enum class BattlePhase { PLAYER_TURN, ENEMY_TURN, RESOLVING, FINISHED }

enum class BattleOutcome { CLEAR, FLED, FELL }

data class BattleItemUi(
    val id: String,
    @StringRes val nameRes: Int,
    val heal: Int,
    val count: Int,
    val blue: Boolean = false
)

data class BattleUiState(
    val heroName: String = "",
    val heroClass: HeroClassUi = HeroClassUi.WARRIOR,
    val heroHp: Int = 0,
    val heroMaxHp: Int = 0,
    val floor: Int = 1,
    val totalFloors: Int = 3,
    val turn: Int = 1,
    val phase: BattlePhase = BattlePhase.RESOLVING,
    @StringRes val enemyNameRes: Int = R.string.battle_enemy_goblin,
    @StringRes val enemyDescriptionRes: Int = R.string.battle_enemy_goblin_desc,
    val enemyHp: Int = 0,
    val enemyMaxHp: Int = 0,
    @StringRes val logRes: Int = R.string.battle_log_start,
    val logAmount: Int = 0,
    val inventory: List<BattleItemUi> = emptyList(),
    val pendingGold: Int = 0,
    val pendingOfferings: Int = 0,
    val heroAttackSeq: Int = 0,
    val enemyAttackSeq: Int = 0,
    val heroHitSeq: Int = 0,
    val enemyHitSeq: Int = 0,
    // Separate counters let the UI play a death fade and a spawn slide
    // without the ViewModel knowing anything about animation.
    val enemyDeathSeq: Int = 0,
    val enemySpawnSeq: Int = 0,
    val fleeing: Boolean = false,
    val paused: Boolean = false,
    val isLoading: Boolean = true,
    val outcome: BattleOutcome? = null,
    val outcomeGold: Int = 0,
    val outcomeOfferings: Int = 0,
    val outcomeFloor: Int = 0,
    @StringRes val errorRes: Int? = null
)

class BattleViewModel(
    private val userId: Long,
    private val userRepository: UserRepository,
    private val playerProgressRepository: PlayerProgressRepository,
    private val runHistoryRepository: RunHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BattleUiState())
    val uiState: StateFlow<BattleUiState> = _uiState.asStateFlow()

    private var progress: PlayerProgressEntity? = null
    private var stacks: MutableList<InventoryStack> = mutableListOf()
    private var enemyAttack: Int = 0
    private var enemyDefense: Int = 0
    private var busy = false
    private var finished = false

    init {
        viewModelScope.launch {
            try {
                val user = userRepository.findUserById(userId)
                val hero = playerProgressRepository.findByUser(userId)
                if (hero == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phase = BattlePhase.FINISHED,
                            outcome = BattleOutcome.FELL,
                            outcomeFloor = 1,
                            errorRes = R.string.battle_error_no_hero
                        )
                    }
                    return@launch
                }

                progress = hero
                stacks = InventoryCodec.decode(hero.inventoryJson).toMutableList()
                _uiState.update {
                    it.copy(
                        heroName = user?.name.orEmpty(),
                        heroClass = HeroClassUi.fromId(hero.heroClass),
                        heroHp = hero.hp,
                        heroMaxHp = hero.maxHp,
                        inventory = toItems(),
                        isLoading = false
                    )
                }
                startFloor(1)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        phase = BattlePhase.FINISHED,
                        outcome = BattleOutcome.FELL,
                        outcomeFloor = 1,
                        errorRes = R.string.error_unknown
                    )
                }
            }
        }
    }

    fun onAttack() {
        if (!canAct()) return
        val hero = progress ?: return
        val damage = CombatEngine.basicDamage(hero.attackDamage, enemyDefense)
        applyHeroDamage(damage, R.string.battle_log_hero_attack)
    }

    fun onSpecial() {
        if (!canAct()) return
        val hero = progress ?: return
        when (_uiState.value.heroClass) {
            HeroClassUi.WARRIOR -> {
                val roll = CombatEngine.warriorSpecial(hero.attackDamage, enemyDefense)
                if (roll.hit) {
                    applyHeroDamage(roll.damage, R.string.battle_log_hero_special_warrior)
                } else {
                    _uiState.update {
                        it.copy(
                            heroAttackSeq = it.heroAttackSeq + 1,
                            logRes = R.string.battle_log_hero_special_miss,
                            logAmount = 0
                        )
                    }
                    beginEnemyTurn()
                }
            }

            HeroClassUi.ARCHER -> {
                val roll = CombatEngine.archerSpecial(hero.attackDamage, enemyDefense)
                if (roll.hit) {
                    applyHeroDamage(roll.damage, R.string.battle_log_hero_archer)
                } else {
                    _uiState.update {
                        it.copy(
                            heroAttackSeq = it.heroAttackSeq + 1,
                            logRes = R.string.battle_log_archer_miss,
                            logAmount = 0
                        )
                    }
                    beginEnemyTurn()
                }
            }
        }
    }

    fun onUseItem(itemId: String) {
        if (!canAct()) return
        val stack = stacks.firstOrNull { it.id == itemId && it.count > 0 } ?: return
        val item = ItemCatalog.byId(stack.id) ?: return
        val state = _uiState.value

        if (state.heroHp >= state.heroMaxHp) {
            _uiState.update {
                it.copy(logRes = R.string.battle_log_full_hp, logAmount = 0)
            }
            return
        }

        val healed = minOf(item.heal, state.heroMaxHp - state.heroHp)
        stacks = stacks
            .map { existing ->
                if (existing.id == itemId) existing.copy(count = existing.count - 1) else existing
            }
            .filter { it.count > 0 }
            .toMutableList()

        _uiState.update {
            it.copy(
                heroHp = state.heroHp + healed,
                inventory = toItems(),
                logRes = R.string.battle_log_heal,
                logAmount = healed
            )
        }
        beginEnemyTurn()
    }

    fun onFlee() {
        if (!canAct()) return
        if (CombatEngine.fleeSucceeds()) {
            busy = true
            _uiState.update {
                it.copy(
                    fleeing = true,
                    phase = BattlePhase.RESOLVING,
                    logRes = R.string.battle_log_flee_success,
                    logAmount = 0
                )
            }
            viewModelScope.launch {
                delay(900)
                finish(BattleOutcome.FLED)
            }
        } else {
            _uiState.update {
                it.copy(logRes = R.string.battle_log_flee_fail, logAmount = 0)
            }
            beginEnemyTurn()
        }
    }

    fun onPause() {
        val state = _uiState.value
        // Pausing is only safe between turns; otherwise an enemy coroutine
        // would keep resolving damage behind the overlay.
        if (finished || busy || state.outcome != null || state.phase != BattlePhase.PLAYER_TURN) return
        _uiState.update { it.copy(paused = true) }
    }

    fun onResume() {
        _uiState.update { it.copy(paused = false) }
    }

    fun onAbandon() {
        if (finished || _uiState.value.outcome != null) return
        _uiState.update {
            it.copy(
                paused = false,
                fleeing = true,
                phase = BattlePhase.RESOLVING,
                logRes = R.string.battle_log_abandon,
                logAmount = 0
            )
        }
        viewModelScope.launch {
            delay(500)
            finish(BattleOutcome.FLED)
        }
    }

    private fun applyHeroDamage(damage: Int, @StringRes logRes: Int) {
        busy = true
        val state = _uiState.value
        val newHp = (state.enemyHp - damage).coerceAtLeast(0)
        _uiState.update {
            it.copy(
                enemyHp = newHp,
                heroAttackSeq = it.heroAttackSeq + 1,
                enemyHitSeq = if (newHp <= 0) it.enemyHitSeq + 1 else it.enemyHitSeq,
                logRes = logRes,
                logAmount = damage
            )
        }
        if (newHp <= 0) {
            handleEnemyDefeated()
        } else {
            beginEnemyTurn()
        }
    }

    private fun beginEnemyTurn() {
        busy = true
        _uiState.update { it.copy(phase = BattlePhase.ENEMY_TURN) }
        viewModelScope.launch {
            delay(650)
            if (finished || _uiState.value.fleeing) return@launch
            val hero = progress ?: return@launch
            val damage = CombatEngine.basicDamage(enemyAttack, hero.defense)
            val newHp = (_uiState.value.heroHp - damage).coerceAtLeast(0)
            _uiState.update {
                it.copy(
                    heroHp = newHp,
                    enemyAttackSeq = it.enemyAttackSeq + 1,
                    heroHitSeq = it.heroHitSeq + 1,
                    logRes = R.string.battle_log_enemy_attack,
                    logAmount = damage
                )
            }
            delay(450)
            if (finished || _uiState.value.fleeing) return@launch
            if (newHp <= 0) {
                finish(BattleOutcome.FELL)
            } else {
                _uiState.update {
                    it.copy(turn = it.turn + 1, phase = BattlePhase.PLAYER_TURN)
                }
                busy = false
            }
        }
    }

    private fun handleEnemyDefeated() {
        val state = _uiState.value
        val enemy = EnemyCatalog.GOBLIN
        val gold = state.pendingGold + enemy.gold
        val offerings = state.pendingOfferings + enemy.offerings

        _uiState.update {
            it.copy(
                enemyHp = 0,
                pendingGold = gold,
                pendingOfferings = offerings,
                phase = BattlePhase.RESOLVING,
                enemyDeathSeq = it.enemyDeathSeq + 1,
                logRes = R.string.battle_log_enemy_dead,
                logAmount = 0
            )
        }

        if (state.floor >= state.totalFloors) {
            viewModelScope.launch {
                delay(800)
                if (finished || _uiState.value.fleeing) return@launch
                finish(BattleOutcome.CLEAR)
            }
        } else {
            viewModelScope.launch {
                delay(700)
                if (finished || _uiState.value.fleeing) return@launch
                _uiState.update {
                    it.copy(logRes = R.string.battle_log_floor_clear, logAmount = 0)
                }
                delay(600)
                if (finished || _uiState.value.fleeing) return@launch
                startFloor(state.floor + 1)
            }
        }
    }

    private fun startFloor(floor: Int) {
        val enemy = EnemyCatalog.GOBLIN
        val hp = CombatEngine.scaledHp(enemy.maxHp, floor)
        enemyAttack = CombatEngine.scaledAttack(enemy.attack, floor)
        enemyDefense = CombatEngine.scaledDefense(enemy.defense, floor)

        _uiState.update {
            it.copy(
                floor = floor,
                phase = BattlePhase.PLAYER_TURN,
                enemyHp = hp,
                enemyMaxHp = hp,
                enemySpawnSeq = it.enemySpawnSeq + 1,
                logRes = R.string.battle_log_start,
                logAmount = 0
            )
        }
        busy = false
    }

    private fun finish(outcome: BattleOutcome) {
        if (finished) return
        finished = true
        busy = true

        val hero = progress
        val state = _uiState.value
        val gold = when (outcome) {
            BattleOutcome.CLEAR -> state.pendingGold
            BattleOutcome.FLED -> state.pendingGold / 2
            BattleOutcome.FELL -> 0
        }
        val offerings = if (outcome == BattleOutcome.CLEAR) state.pendingOfferings else 0

        viewModelScope.launch {
            try {
                if (hero != null) {
                    when (outcome) {
                        BattleOutcome.CLEAR -> playerProgressRepository.upsert(
                            hero.copy(
                                hp = state.heroHp.coerceAtMost(hero.maxHp),
                                gold = hero.gold + gold,
                                offerings = hero.offerings + offerings,
                                inventoryJson = InventoryCodec.encode(stacks)
                            )
                        )

                        BattleOutcome.FLED -> playerProgressRepository.upsert(
                            hero.copy(
                                hp = state.heroHp.coerceAtMost(hero.maxHp),
                                gold = hero.gold + gold,
                                inventoryJson = InventoryCodec.encode(stacks)
                            )
                        )

                        // Death resets the active hero; the run history survives
                        // through the separate table and the CASCADE FK only
                        // removes progress/skills for the profile.
                        BattleOutcome.FELL -> playerProgressRepository.deleteForUser(userId)
                    }

                    runHistoryRepository.insert(
                        RunHistoryEntity(
                            userId = userId,
                            locationName = DUNGEON_NAME,
                            result = outcome.name,
                            floorReached = state.floor,
                            goldEarned = gold,
                            offeringsEarned = offerings,
                            endedAt = System.currentTimeMillis()
                        )
                    )
                }

                _uiState.update {
                    it.copy(
                        phase = BattlePhase.FINISHED,
                        outcome = outcome,
                        outcomeGold = gold,
                        outcomeOfferings = offerings,
                        outcomeFloor = state.floor,
                        isLoading = false,
                        paused = false,
                        logRes = outcomeLog(outcome),
                        logAmount = 0
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        phase = BattlePhase.FINISHED,
                        outcome = outcome,
                        outcomeGold = gold,
                        outcomeOfferings = offerings,
                        outcomeFloor = state.floor,
                        errorRes = R.string.error_unknown
                    )
                }
            }
        }
    }

    private fun canAct(): Boolean {
        val state = _uiState.value
        return !busy &&
            !finished &&
            !state.isLoading &&
            !state.paused &&
            state.outcome == null &&
            state.phase == BattlePhase.PLAYER_TURN
    }

    private fun toItems(): List<BattleItemUi> = stacks.mapNotNull { stack ->
        val definition = ItemCatalog.byId(stack.id) ?: return@mapNotNull null
        BattleItemUi(
            id = definition.id,
            nameRes = itemNameRes(definition.id),
            heal = definition.heal,
            count = stack.count,
            blue = definition.id == ItemCatalog.BLUE
        )
    }

    private fun itemNameRes(id: String): Int = when (id) {
        ItemCatalog.RED -> R.string.battle_item_red
        ItemCatalog.BLUE -> R.string.battle_item_blue
        else -> R.string.battle_item_unknown
    }

    private fun outcomeLog(outcome: BattleOutcome): Int = when (outcome) {
        BattleOutcome.CLEAR -> R.string.battle_log_victory
        BattleOutcome.FLED -> R.string.battle_log_flee_success
        BattleOutcome.FELL -> R.string.battle_log_fell
    }

    companion object {
        private const val TOTAL_FLOORS = 3
        private const val DUNGEON_NAME = "THE SUNKEN KEEP"

        fun factory(
            userId: Long,
            userRepository: UserRepository,
            playerProgressRepository: PlayerProgressRepository,
            runHistoryRepository: RunHistoryRepository
        ) = viewModelFactory {
            initializer {
                BattleViewModel(
                    userId = userId,
                    userRepository = userRepository,
                    playerProgressRepository = playerProgressRepository,
                    runHistoryRepository = runHistoryRepository
                )
            }
        }
    }
}
