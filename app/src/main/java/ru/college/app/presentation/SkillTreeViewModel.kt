package ru.college.app.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.college.app.R
import ru.college.app.data.local.db.entities.UnlockedSkillEntity
import ru.college.app.data.repository.PlayerProgressRepository
import ru.college.app.data.repository.UnlockedSkillRepository
import ru.college.app.domain.model.HeroClass
import ru.college.app.domain.model.SkillCatalog
import ru.college.app.domain.model.SkillDefinition

data class SkillNodeUi(
    val id: String,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
    val cost: Int,
    val tier: Int,
    val unlocked: Boolean,
    val available: Boolean,
    val affordable: Boolean
)

data class SkillTreeUiState(
    val heroClass: HeroClassUi = HeroClassUi.WARRIOR,
    val offerings: Int = 0,
    val warriorNodes: List<SkillNodeUi> = emptyList(),
    val archerNodes: List<SkillNodeUi> = emptyList(),
    val isLoading: Boolean = true,
    @StringRes val messageRes: Int? = null,
    @StringRes val errorRes: Int? = null
)

class SkillTreeViewModel(
    private val userId: Long,
    private val playerProgressRepository: PlayerProgressRepository,
    private val unlockedSkillRepository: UnlockedSkillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillTreeUiState())
    val uiState: StateFlow<SkillTreeUiState> = _uiState.asStateFlow()

    private var currentOfferings: Int = 0
    private var currentHeroClass: HeroClass = HeroClass.WARRIOR
    private var currentUnlocked: Set<String> = emptySet()
    private var actionInFlight = false

    init {
        viewModelScope.launch {
            combine(
                playerProgressRepository.observeByUser(userId),
                unlockedSkillRepository.observeByUser(userId)
            ) { progress, skills ->
                val unlockedIds = skills.map { it.skillId }.toSet()
                Triple(progress, unlockedIds, progress?.heroClass)
            }.collect { (progress, unlockedIds, heroClassId) ->
                currentUnlocked = unlockedIds
                currentOfferings = progress?.offerings ?: 0
                currentHeroClass = HeroClass.fromId(heroClassId)

                _uiState.update {
                    it.copy(
                        heroClass = HeroClassUi.fromId(heroClassId),
                        offerings = currentOfferings,
                        warriorNodes = buildNodes(HeroClass.WARRIOR, unlockedIds, currentOfferings),
                        archerNodes = buildNodes(HeroClass.ARCHER, unlockedIds, currentOfferings),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onUnlock(skillId: String) {
        if (actionInFlight) return

        val definition = SkillCatalog.byId(skillId) ?: return
        if (definition.heroClass != currentHeroClass) {
            notice(R.string.skilltree_msg_wrong_class)
            return
        }
        if (skillId in currentUnlocked) return
        if (!SkillCatalog.prerequisitesMet(definition, currentUnlocked)) {
            notice(R.string.skilltree_msg_locked)
            return
        }
        if (currentOfferings < definition.cost) {
            notice(R.string.skilltree_msg_need_offerings)
            return
        }

        actionInFlight = true
        viewModelScope.launch {
            try {
                // Record the oath first: the unique index makes this idempotent.
                // A conflict returns -1, so bonuses are never applied twice.
                val inserted = unlockedSkillRepository.unlock(
                    UnlockedSkillEntity(
                        userId = userId,
                        skillId = skillId,
                        unlockedAt = System.currentTimeMillis()
                    )
                )
                if (inserted != -1L) {
                    val progress = playerProgressRepository.findByUser(userId)
                    if (progress != null) {
                        // Permanent bonuses are baked into the hero once, so combat
                        // and the city always read the same numbers.
                        playerProgressRepository.upsert(
                            progress.copy(
                                offerings = (progress.offerings - definition.cost)
                                    .coerceAtLeast(0),
                                attackDamage = progress.attackDamage + definition.attackBonus,
                                defense = progress.defense + definition.defenseBonus,
                                maxHp = progress.maxHp + definition.maxHpBonus,
                                hp = progress.hp + definition.maxHpBonus
                            )
                        )
                    }
                }
                _uiState.update {
                    it.copy(
                        messageRes = R.string.skilltree_msg_unlocked,
                        errorRes = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(errorRes = R.string.error_unknown) }
            } finally {
                actionInFlight = false
            }
        }
    }

    private fun buildNodes(
        heroClass: HeroClass,
        unlockedIds: Set<String>,
        offerings: Int
    ): List<SkillNodeUi> = SkillCatalog.forClass(heroClass).map { definition ->
        val unlocked = definition.id in unlockedIds
        val available = !unlocked && SkillCatalog.prerequisitesMet(definition, unlockedIds)
        definition.toUi(
            unlocked = unlocked,
            available = available,
            affordable = available && offerings >= definition.cost
        )
    }

    private fun SkillDefinition.toUi(
        unlocked: Boolean,
        available: Boolean,
        affordable: Boolean
    ) = SkillNodeUi(
        id = id,
        nameRes = nameRes(id),
        descriptionRes = descriptionRes(id),
        cost = cost,
        tier = tier,
        unlocked = unlocked,
        available = available,
        affordable = affordable
    )

    private fun notice(@StringRes messageRes: Int) {
        _uiState.update { it.copy(messageRes = messageRes, errorRes = null) }
    }

    private fun nameRes(id: String): Int = when (id) {
        "IRON_GUARD" -> R.string.skill_iron_guard_name
        "CRUSHING_BLOW" -> R.string.skill_crushing_blow_name
        "BLOOD_OATH" -> R.string.skill_blood_oath_name
        "STEADY_AIM" -> R.string.skill_steady_aim_name
        "TWIN_SHOT" -> R.string.skill_twin_shot_name
        "SHADOW_STEP" -> R.string.skill_shadow_step_name
        else -> R.string.skill_unknown_name
    }

    private fun descriptionRes(id: String): Int = when (id) {
        "IRON_GUARD" -> R.string.skill_iron_guard_desc
        "CRUSHING_BLOW" -> R.string.skill_crushing_blow_desc
        "BLOOD_OATH" -> R.string.skill_blood_oath_desc
        "STEADY_AIM" -> R.string.skill_steady_aim_desc
        "TWIN_SHOT" -> R.string.skill_twin_shot_desc
        "SHADOW_STEP" -> R.string.skill_shadow_step_desc
        else -> R.string.skill_unknown_desc
    }

    companion object {
        fun factory(
            userId: Long,
            playerProgressRepository: PlayerProgressRepository,
            unlockedSkillRepository: UnlockedSkillRepository
        ) = viewModelFactory {
            initializer {
                SkillTreeViewModel(
                    userId = userId,
                    playerProgressRepository = playerProgressRepository,
                    unlockedSkillRepository = unlockedSkillRepository
                )
            }
        }
    }
}
