package com.example.studypartner

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

object UserPreferences {

    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    private val DEFAULT_DIFFICULTY    = intPreferencesKey("default_difficulty")
    private val DEFAULT_URGENCY       = intPreferencesKey("default_urgency")
    private val ONBOARDING_DONE       = booleanPreferencesKey("onboarding_done")
    private val TUNISIAN_MODE         = booleanPreferencesKey("tunisian_mode")
    private val PANIC_MODE_COUNT      = intPreferencesKey("panic_mode_count")
    private val LOGGED_IN             = booleanPreferencesKey("logged_in")
    private val EMAIL_VERIFIED        = booleanPreferencesKey("email_verified")
    private val ACCOUNT_EMAIL         = stringPreferencesKey("account_email")
    private val THEME_MODE            = stringPreferencesKey("theme_mode")
    private val OPENROUTER_API_KEY    = stringPreferencesKey("openrouter_api_key")
    private val AWS_ACCESS_KEY        = stringPreferencesKey("aws_access_key")
    private val AWS_SECRET_KEY        = stringPreferencesKey("aws_secret_key")
    private val AWS_REGION            = stringPreferencesKey("aws_region")

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    // ── Read ──────────────────────────────────────────────────────────────────

    fun notificationsEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }

    fun defaultDifficulty(context: Context): Flow<Int> =
        context.dataStore.data.map { it[DEFAULT_DIFFICULTY] ?: Level.LOW.value }

    fun defaultUrgency(context: Context): Flow<Int> =
        context.dataStore.data.map { it[DEFAULT_URGENCY] ?: Level.LOW.value }

    fun onboardingDone(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[ONBOARDING_DONE] ?: false }

    fun tunisianMode(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[TUNISIAN_MODE] ?: false }

    fun panicModeCount(context: Context): Flow<Int> =
        context.dataStore.data.map { it[PANIC_MODE_COUNT] ?: 0 }

    fun loggedIn(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[LOGGED_IN] ?: false }

    fun emailVerified(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[EMAIL_VERIFIED] ?: false }

    fun accountEmail(context: Context): Flow<String> =
        context.dataStore.data.map { it[ACCOUNT_EMAIL] ?: "" }

    fun themeMode(context: Context): Flow<String> =
        context.dataStore.data.map { it[THEME_MODE] ?: THEME_SYSTEM }

    fun openRouterApiKey(context: Context): Flow<String> =
        context.dataStore.data.map { it[OPENROUTER_API_KEY] ?: "" }

    fun awsAccessKey(context: Context): Flow<String> =
        context.dataStore.data.map { it[AWS_ACCESS_KEY] ?: "" }

    fun awsSecretKey(context: Context): Flow<String> =
        context.dataStore.data.map { it[AWS_SECRET_KEY] ?: "" }

    fun awsRegion(context: Context): Flow<String> =
        context.dataStore.data.map { it[AWS_REGION] ?: "us-east-1" }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setDefaultDifficulty(context: Context, value: Int) {
        context.dataStore.edit { it[DEFAULT_DIFFICULTY] = value }
    }

    suspend fun setDefaultUrgency(context: Context, value: Int) {
        context.dataStore.edit { it[DEFAULT_URGENCY] = value }
    }

    suspend fun setOnboardingDone(context: Context) {
        context.dataStore.edit { it[ONBOARDING_DONE] = true }
    }

    suspend fun setTunisianMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[TUNISIAN_MODE] = enabled }
    }

    suspend fun incrementPanicCount(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[PANIC_MODE_COUNT] = (prefs[PANIC_MODE_COUNT] ?: 0) + 1
        }
    }

    suspend fun setPendingRegistration(context: Context, email: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCOUNT_EMAIL] = email
            prefs[LOGGED_IN] = false
            prefs[EMAIL_VERIFIED] = false
        }
    }

    suspend fun setLoggedIn(context: Context, email: String? = null) {
        context.dataStore.edit { prefs ->
            val previousEmail = prefs[ACCOUNT_EMAIL]
            prefs[LOGGED_IN] = true
            if (!email.isNullOrBlank()) {
                if (previousEmail != null && previousEmail != email) {
                    prefs[EMAIL_VERIFIED] = false
                }
                prefs[ACCOUNT_EMAIL] = email
            }
        }
    }

    suspend fun setEmailVerified(context: Context, verified: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[EMAIL_VERIFIED] = verified
        }
    }

    suspend fun setAccountEmail(context: Context, email: String) {
        context.dataStore.edit { prefs ->
            if (prefs[ACCOUNT_EMAIL] != email) {
                prefs[EMAIL_VERIFIED] = false
            }
            prefs[ACCOUNT_EMAIL] = email
        }
    }

    suspend fun logout(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[LOGGED_IN] = false
        }
    }

    suspend fun setThemeMode(context: Context, mode: String) {
        val allowed = setOf(THEME_SYSTEM, THEME_LIGHT, THEME_DARK)
        val normalized = if (mode in allowed) mode else THEME_SYSTEM
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = normalized
        }
    }

    suspend fun setOpenRouterApiKey(context: Context, key: String) {
        context.dataStore.edit { it[OPENROUTER_API_KEY] = key }
    }

    suspend fun setAwsCredentials(context: Context, accessKey: String, secretKey: String, region: String) {
        context.dataStore.edit {
            it[AWS_ACCESS_KEY] = accessKey
            it[AWS_SECRET_KEY] = secretKey
            it[AWS_REGION] = region
        }
    }
}
