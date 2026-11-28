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
import ru.college.app.data.local.db.entities.PlayerProgressEntity
import ru.college.app.data.local.db.entities.RunHistoryEntity
import ru.college.app.data.repository.PlayerProgressRepository
import ru.college.app.data.repository.RunHistoryRepository
import ru.college.app.data.repository.UserRepository
import ru.college.app.domain.model.HeroClass

/** UI-level mirror of [HeroClass] so Composables never import domain classes. */
enum class MainClassOption(val heroClass: HeroClass) {
    WARRIOR(HeroClass.WARRIOR),
    ARCHER(HeroClass.ARCHER)
}

enum class RunResultUi { CLEAR, FELL, FLED }

data class RunHistoryUi(
    val id: Long,
    val locationName: String,
    val result: RunResultUi,
    val floorReached: Int
)

data class MainUiState(
    val playerName: String = "",
    val soulLevel: Int = 1,
    val totalRuns: Int = 0,
    val runs: List<RunHistoryUi> = emptyList(),
    val selectedClass: MainClassOption = MainClassOption.WARRIOR,
    val isLoading: Boolean = true,
    val navigateToCity: Boolean = false,
    @StringRes val errorRes: Int? = null
)

class MainViewModel(
    private val userId: Long,
    private val userRepository: UserRepository,
    private val runHistoryRepository: RunHistoryRepository,
    private val playerProgressRepository: PlayerProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
        observeRuns()
        observeHero()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                _uiState.update {
                    it.copy(
                        playerName = user?.name ?: "",
                        soulLevel = user?.soulLevel ?: 1,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeRuns() {
        viewModelScope.launch {
            runHistoryRepository.observeByUser(userId).collect { runs ->
                _uiState.update {
                    it.copy(
                        runs = runs.map(::toUi),
                        totalRuns = runs.size
                    )
                }
            }
        }
    }

    private fun observeHero() {
        viewModelScope.launch {
            playerProgressRepository.observeByUser(userId).collect { progress ->
                if (progress != null) {
                    val option = MainClassOption.entries
                        .firstOrNull { it.heroClass.id == progress.heroClass }
                        ?: MainClassOption.WARRIOR
                    _uiState.update { it.copy(selectedClass = option) }
                }
            }
        }
    }

    fun onClassSelected(option: MainClassOption) {
        _uiState.update { it.copy(selectedClass = option) }
    }

    fun onPlay() {
        val current = _uiState.value
        if (current.isLoading) return

        viewModelScope.launch {
            try {
                val heroClass = current.selectedClass.heroClass
                val existing = playerProgressRepository.findByUser(userId)
                if (existing == null) {
                    playerProgressRepository.upsert(newHero(heroClass))
                } else if (existing.heroClass != heroClass.id) {
                    // Switching class reforges the body but keeps the soul's wealth.
                    playerProgressRepository.upsert(
                        existing.copy(
                            heroClass = heroClass.id,
                            hp = heroClass.baseHp,
                            maxHp = heroClass.baseHp,
                            attackDamage = heroClass.baseAttack,
                            defense = heroClass.baseDefense
                        )
                    )
                }
                _uiState.update { it.copy(navigateToCity = true) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(errorRes = R.string.error_unknown) }
            }
        }
    }

    fun onNavigationHandled() {
        _uiState.update { it.copy(navigateToCity = false) }
    }

    private fun newHero(heroClass: HeroClass) = PlayerProgressEntity(
        userId = userId,
        heroClass = heroClass.id,
        hp = heroClass.baseHp,
        maxHp = heroClass.baseHp,
        attackDamage = heroClass.baseAttack,
        defense = heroClass.baseDefense,
        // A small starting purse lets a fresh profile try the Armory and the
        // Shrine immediately instead of staring at disabled buttons.
        gold = 60,
        offerings = 2,
        weaponLevel = 0,
        armorLevel = 0,
        inventoryJson = "[]"
    )

    private fun toUi(run: RunHistoryEntity) = RunHistoryUi(
        id = run.id,
        locationName = run.locationName,
        result = when (run.result) {
            "CLEAR" -> RunResultUi.CLEAR
            "FLED" -> RunResultUi.FLED
            else -> RunResultUi.FELL
        },
        floorReached = run.floorReached
    )

    companion object {
        fun factory(
            userId: Long,
            userRepository: UserRepository,
            runHistoryRepository: RunHistoryRepository,
            playerProgressRepository: PlayerProgressRepository
        ) = viewModelFactory {
            initializer {
                MainViewModel(
                    userId = userId,
                    userRepository = userRepository,
                    runHistoryRepository = runHistoryRepository,
                    playerProgressRepository = playerProgressRepository
                )
            }
        }
    }
}
