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
import ru.college.app.data.local.db.entities.UserEntity
import ru.college.app.data.local.pref.SessionManager
import ru.college.app.data.repository.UserRepository
import java.security.MessageDigest
import java.security.SecureRandom

enum class AuthMode { LOGIN, REGISTER }

data class LoginUiState(
    val mode: AuthMode = AuthMode.REGISTER,
    val email: String = "",
    val password: String = "",
    val playerName: String = "",
    @StringRes val emailErrorRes: Int? = null,
    @StringRes val passwordErrorRes: Int? = null,
    @StringRes val playerNameErrorRes: Int? = null,
    @StringRes val formErrorRes: Int? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userId: Long? = null
)

class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onModeSelected(mode: AuthMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                emailErrorRes = null,
                passwordErrorRes = null,
                playerNameErrorRes = null,
                formErrorRes = null
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailErrorRes = null, formErrorRes = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordErrorRes = null, formErrorRes = null) }
    }

    fun onPlayerNameChange(value: String) {
        _uiState.update {
            it.copy(playerName = value, playerNameErrorRes = null, formErrorRes = null)
        }
    }

    /**
     * Single entry point for both LOGIN and REGISTER. Validation lives here
     * (never in Composables) so it can be unit-tested without Android.
     */
    fun onSubmit() {
        if (_uiState.value.isLoading) return

        val current = _uiState.value
        val email = current.email.trim().lowercase()
        val name = current.playerName.trim()

        val emailError = if (!isValidEmail(email)) R.string.error_email_invalid else null
        val passwordError =
            if (current.password.length < MIN_PASSWORD_LENGTH) R.string.error_password_short else null
        val nameError = if (current.mode == AuthMode.REGISTER && !isValidPlayerName(name)) {
            R.string.error_player_name_invalid
        } else {
            null
        }

        if (emailError != null || passwordError != null || nameError != null) {
            _uiState.update {
                it.copy(
                    emailErrorRes = emailError,
                    passwordErrorRes = passwordError,
                    playerNameErrorRes = nameError,
                    formErrorRes = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, formErrorRes = null) }
            try {
                val success = when (current.mode) {
                    AuthMode.REGISTER -> register(email, name, current.password)
                    AuthMode.LOGIN -> login(email, current.password)
                }
                if (success) {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, formErrorRes = R.string.error_unknown)
                }
            }
        }
    }

    private suspend fun register(email: String, name: String, password: String): Boolean {
        if (userRepository.isEmailTaken(email)) {
            _uiState.update { it.copy(formErrorRes = R.string.error_email_taken) }
            return false
        }
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        val userId = userRepository.insertUser(
            UserEntity(
                email = email,
                name = name,
                passwordHash = hash,
                passwordSalt = salt,
                createdAt = System.currentTimeMillis()
            )
        )
        sessionManager.saveSession(userId)
        _uiState.update { it.copy(userId = userId) }
        return true
    }

    private suspend fun login(email: String, password: String): Boolean {
        val user = userRepository.findUserByEmail(email)
        if (user == null || hashPassword(password, user.passwordSalt) != user.passwordHash) {
            _uiState.update { it.copy(formErrorRes = R.string.error_invalid_credentials) }
            return false
        }
        sessionManager.saveSession(user.id)
        _uiState.update { it.copy(userId = user.id) }
        return true
    }

    private fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

    private fun isValidPlayerName(name: String): Boolean =
        name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH && PLAYER_NAME_REGEX.matches(name)

    private fun generateSalt(): String {
        val bytes = ByteArray(SALT_SIZE_BYTES)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    private fun hashPassword(password: String, salt: String): String {
        // SHA-256 over (salt bytes || password bytes); the salt is stored next to the hash.
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.hexToBytes())
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.toHex()
    }

    private fun ByteArray.toHex(): String = joinToString(separator = "") { byte -> "%02x".format(byte) }

    private fun String.hexToBytes(): ByteArray =
        chunked(2).map { pair -> pair.toInt(16).toByte() }.toByteArray()

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private const val MIN_NAME_LENGTH = 3
        private const val MAX_NAME_LENGTH = 20
        private const val SALT_SIZE_BYTES = 16

        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val PLAYER_NAME_REGEX = Regex("^[A-Za-zА-Яа-яЁё0-9 _]+$")

        fun factory(
            userRepository: UserRepository,
            sessionManager: SessionManager
        ) = viewModelFactory {
            initializer {
                LoginViewModel(
                    userRepository = userRepository,
                    sessionManager = sessionManager
                )
            }
        }
    }
}
