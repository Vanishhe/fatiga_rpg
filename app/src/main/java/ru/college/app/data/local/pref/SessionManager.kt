package ru.college.app.data.local.pref

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tiny SharedPreferences wrapper for the active session and local settings.
 * Keeping it out of ViewModels means both Login and Settings share one source
 * of truth without adding a DI framework (Hilt is forbidden by the spec).
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Exposed as a flow so the root composable can swap the resources locale
    // immediately when the player changes the language in Settings.
    private val _language = MutableStateFlow(
        prefs.getString(KEY_LANGUAGE, LANGUAGE_EN) ?: LANGUAGE_EN
    )
    val language: StateFlow<String> = _language.asStateFlow()

    var userId: Long?
        get() = prefs.getLong(KEY_USER_ID, NO_USER).takeIf { it != NO_USER }
        private set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_USER_ID) else putLong(KEY_USER_ID, value)
            }.apply()
        }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var languageCode: String
        get() = _language.value
        set(value) {
            val normalized = if (value == LANGUAGE_RU) LANGUAGE_RU else LANGUAGE_EN
            prefs.edit().putString(KEY_LANGUAGE, normalized).apply()
            _language.value = normalized
        }

    fun saveSession(userId: Long) {
        this.userId = userId
    }

    fun clearSession() {
        this.userId = null
    }

    companion object {
        const val LANGUAGE_EN = "en"
        const val LANGUAGE_RU = "ru"

        private const val PREFS_NAME = "fatiga_session"
        private const val KEY_USER_ID = "active_user_id"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_LANGUAGE = "language"
        private const val NO_USER = -1L
    }
}
