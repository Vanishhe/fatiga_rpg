package ru.college.app.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.college.app.R
import ru.college.app.data.local.db.InventoryCodec
import ru.college.app.data.local.db.InventoryStack
import ru.college.app.data.local.db.entities.PlayerProgressEntity
import ru.college.app.data.repository.PlayerProgressRepository
import ru.college.app.data.repository.UserRepository
import ru.college.app.domain.model.ItemCatalog

data class CityUiState(
    val playerName: String = "",
    val heroClass: HeroClassUi = HeroClassUi.WARRIOR,
    val soulLevel: Int = 1,
    val hp: Int = 0,
    val maxHp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val gold: Int = 0,
    val offerings: Int = 0,
    val weaponLevel: Int = 0,
    val armorLevel: Int = 0,
    val redPotions: Int = 0,
    val bluePotions: Int = 0,
    val redPotionPrice: Int = ItemCatalog.red.price,
    val bluePotionPrice: Int = ItemCatalog.blue.price,
    val isLoading: Boolean = true,
    val armoryOpen: Boolean = false,
    @StringRes val messageRes: Int? = null,
    val navigateToSkillTree: Boolean = false,
    val navigateToBattle: Boolean = false,
    @StringRes val errorRes: Int? = null
) {
    val weaponCost: Int get() = 50 + 40 * weaponLevel
    val armorCost: Int get() = 50 + 40 * armorLevel
}

class CityViewModel(
    private val userId: Long,
    private val userRepository: UserRepository,
    private val playerProgressRepository: PlayerProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CityUiState())
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()

    private var currentProgress: PlayerProgressEntity? = null
    private var actionInFlight = false

    init {
        observeUser()
        observeProgress()
    }

    private fun observeUser() {
        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                _uiState.update {
                    it.copy(
                        playerName = user?.name.orEmpty(),
                        soulLevel = user?.soulLevel ?: 1
                    )
                }
            }
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            playerProgressRepository.observeByUser(userId).collect { progress ->
                currentProgress = progress
                if (progress == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }
                val inventory = InventoryCodec.decode(progress.inventoryJson)
                _uiState.update {
                    it.copy(
                        heroClass = HeroClassUi.fromId(progress.heroClass),
                        hp = progress.hp,
                        maxHp = progress.maxHp,
                        attack = progress.attackDamage,
                        defense = progress.defense,
                        gold = progress.gold,
                        offerings = progress.offerings,
                        weaponLevel = progress.weaponLevel,
                        armorLevel = progress.armorLevel,
                        redPotions = inventory
                            .firstOrNull { stack -> stack.id == ItemCatalog.RED }
                            ?.count ?: 0,
                        bluePotions = inventory
                            .firstOrNull { stack -> stack.id == ItemCatalog.BLUE }
                            ?.count ?: 0,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onShrine() {
        if (currentProgress == null) {
            notice(R.string.city_msg_no_hero)
            return
        }
        _uiState.update { it.copy(navigateToSkillTree = true) }
    }

    fun onToggleArmory() {
        _uiState.update { it.copy(armoryOpen = !it.armoryOpen) }
    }

    fun onPortal() {
        if (currentProgress == null) {
            notice(R.string.city_msg_no_hero)
            return
        }
        _uiState.update { it.copy(navigateToBattle = true) }
    }

    fun onUpgradeWeapon() = mutate { progress ->
        val cost = 50 + 40 * progress.weaponLevel
        if (progress.gold < cost) {
            notice(R.string.city_msg_not_enough_gold)
            return@mutate null
        }
        progress.copy(
            gold = progress.gold - cost,
            attackDamage = progress.attackDamage + WEAPON_ATTACK_BONUS,
            weaponLevel = progress.weaponLevel + 1
        ) to R.string.city_msg_weapon
    }

    fun onUpgradeArmor() = mutate { progress ->
        val cost = 50 + 40 * progress.armorLevel
        if (progress.gold < cost) {
            notice(R.string.city_msg_not_enough_gold)
            return@mutate null
        }
        progress.copy(
            gold = progress.gold - cost,
            defense = progress.defense + ARMOR_DEFENSE_BONUS,
            maxHp = progress.maxHp + ARMOR_HP_BONUS,
            hp = progress.hp + ARMOR_HP_BONUS,
            armorLevel = progress.armorLevel + 1
        ) to R.string.city_msg_armor
    }

    fun onBuyPotion(itemId: String) = mutate { progress ->
        val item = ItemCatalog.byId(itemId)
        if (item == null) return@mutate null
        if (progress.gold < item.price) {
            notice(R.string.city_msg_not_enough_gold)
            return@mutate null
        }

        val stacks = InventoryCodec.decode(progress.inventoryJson).toMutableList()
        val existingIndex = stacks.indexOfFirst { it.id == item.id }
        when {
            existingIndex >= 0 -> {
                val stack = stacks[existingIndex]
                stacks[existingIndex] = stack.copy(count = stack.count + 1)
            }

            stacks.size < MAX_INVENTORY_SLOTS -> {
                stacks.add(InventoryStack(id = item.id, count = 1))
            }

            else -> {
                notice(R.string.city_msg_inventory_full)
                return@mutate null
            }
        }

        progress.copy(
            gold = progress.gold - item.price,
            inventoryJson = InventoryCodec.encode(stacks)
        ) to R.string.city_msg_potion
    }

    fun onNavigationHandled() {
        _uiState.update {
            it.copy(navigateToSkillTree = false, navigateToBattle = false)
        }
    }

    /**
     * Shared guard for every paid action: one request at a time, always based
     * on the latest Room snapshot, never on a stale UI copy.
     */
    private fun mutate(
        block: (PlayerProgressEntity) -> Pair<PlayerProgressEntity, Int>?
    ) {
        if (actionInFlight) return
        val progress = currentProgress ?: run {
            notice(R.string.city_msg_no_hero)
            return
        }

        val result = block(progress) ?: return
        actionInFlight = true
        viewModelScope.launch {
            try {
                playerProgressRepository.upsert(result.first)
                _uiState.update { it.copy(messageRes = result.second, errorRes = null) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(errorRes = R.string.error_unknown) }
            } finally {
                actionInFlight = false
            }
        }
    }

    private fun notice(@StringRes messageRes: Int) {
        _uiState.update { it.copy(messageRes = messageRes, errorRes = null) }
    }

    companion object {
        private const val MAX_INVENTORY_SLOTS = 8
        private const val WEAPON_ATTACK_BONUS = 2
        private const val ARMOR_DEFENSE_BONUS = 1
        private const val ARMOR_HP_BONUS = 4

        fun factory(
            userId: Long,
            userRepository: UserRepository,
            playerProgressRepository: PlayerProgressRepository
        ) = viewModelFactory {
            initializer {
                CityViewModel(
                    userId = userId,
                    userRepository = userRepository,
                    playerProgressRepository = playerProgressRepository
                )
            }
        }
    }
}
