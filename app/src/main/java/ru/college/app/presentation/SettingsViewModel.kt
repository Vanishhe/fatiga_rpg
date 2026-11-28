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
import ru.college.app.data.local.pref.SessionManager
import ru.college.app.data.repository.UserRepository

data class SettingsUiState(
    val name: String = "",
    val savedName: String = "",
    val email: String = "",
    val soulLevel: Int = 1,
    val createdAt: Long = 0L,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    @StringRes val messageRes: Int? = null,
    @StringRes val errorRes: Int? = null,
    val loggedOut: Boolean = false
) {
    val hasChanges: Boolean get() = name.trim() != savedName
}

class SettingsViewModel(
    private val userId: Long,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            soundEnabled = sessionManager.soundEnabled,
            vibrationEnabled = sessionManager.vibrationEnabled
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                _uiState.update { state ->
                    state.copy(
                        // Only hydrate the editable field on the first emission;
                        // later Room updates must not overwrite what the user types.
                        name = if (state.isLoading) user?.name.orEmpty() else state.name,
                        savedName = user?.name.orEmpty(),
                        email = user?.email.orEmpty(),
                        soulLevel = user?.soulLevel ?: 1,
                        createdAt = user?.createdAt ?: 0L,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorRes = null, messageRes = null) }
    }

    fun onToggleSound(enabled: Boolean) {
        sessionManager.soundEnabled = enabled
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun onToggleVibration(enabled: Boolean) {
        sessionManager.vibrationEnabled = enabled
        _uiState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun onSave() {
        val current = _uiState.value
        val trimmed = current.name.trim()
        if (trimmed.length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            _uiState.update { it.copy(errorRes = R.string.settings_error_name) }
            return
        }
        if (current.isSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, errorRes = null, messageRes = null)
            }
            try {
                userRepository.updateProfile(userId, trimmed, current.soulLevel)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        name = trimmed,
                        savedName = trimmed,
                        messageRes = R.string.settings_saved
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorRes = R.string.error_unknown)
                }
            }
        }
    }

    fun onDeleteRequested() {
        _uiState.update { it.copy(showDeleteConfirm = true, errorRes = null, messageRes = null) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            try {
                userRepository.deleteUser(userId)
                sessionManager.clearSession()
                _uiState.update {
                    it.copy(showDeleteConfirm = false, loggedOut = true)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(showDeleteConfirm = false, errorRes = R.string.error_unknown)
                }
            }
        }
    }

    fun onLogout() {
        sessionManager.clearSession()
        _uiState.update { it.copy(loggedOut = true) }
    }

    companion object {
        private const val MIN_NAME_LENGTH = 3
        private const val MAX_NAME_LENGTH = 20

        fun factory(
            userId: Long,
            userRepository: UserRepository,
            sessionManager: SessionManager
        ) = viewModelFactory {
            initializer {
                SettingsViewModel(
                    userId = userId,
                    userRepository = userRepository,
                    sessionManager = sessionManager
                )
            }
        }
    }
}
