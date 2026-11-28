package ru.college.app.data.local.pref

import android.content.Context

/**
 * Tiny SharedPreferences wrapper for the active session and local settings.
 * Keeping it out of ViewModels means both Login and Settings share one source
 * of truth without adding a DI framework (Hilt is forbidden by the spec).
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

    fun saveSession(userId: Long) {
        this.userId = userId
    }

    fun clearSession() {
        this.userId = null
    }

    private companion object {
        const val PREFS_NAME = "fatiga_session"
        const val KEY_USER_ID = "active_user_id"
        const val KEY_SOUND = "sound_enabled"
        const val KEY_VIBRATION = "vibration_enabled"
        const val NO_USER = -1L
    }
}
