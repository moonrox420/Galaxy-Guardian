package com.example.galaxyguardian.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.galaxyguardian.data.model.BotPersonality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bot_personality_prefs")

class PersonalityDataStore(private val context: Context) {

    private object Keys {
        val ID = stringPreferencesKey("personality_id")
        val NAME = stringPreferencesKey("personality_name")
        val TONE = stringPreferencesKey("personality_tone")
        val TRAITS = stringPreferencesKey("personality_traits")
        val SYSTEM_INSTRUCTIONS = stringPreferencesKey("personality_system_instructions")
        val NEGATIVE_CONSTRAINTS = stringPreferencesKey("personality_negative_constraints")
        val RESPONSE_STYLE = stringPreferencesKey("personality_response_style")
        val CREATIVITY_TEMP = floatPreferencesKey("personality_creativity_temp")
        val AVATAR_TYPE = stringPreferencesKey("personality_avatar_type")
        val AVATAR_VALUE = stringPreferencesKey("personality_avatar_value")
        val AVATAR_COLOR_INDEX = intPreferencesKey("personality_avatar_color_index")
        val THEME_MODE = stringPreferencesKey("app_theme_mode")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
        .catch { exception ->
            android.util.Log.e("PersonalityDataStore", "Error reading theme mode", exception)
            emit(emptyPreferences())
        }
        .map { prefs ->
            val modeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(modeStr)
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }
        }

    suspend fun saveThemeMode(mode: ThemeMode) {
        try {
            context.dataStore.edit { prefs ->
                prefs[Keys.THEME_MODE] = mode.name
            }
        } catch (e: Throwable) {
            android.util.Log.e("PersonalityDataStore", "Error writing theme mode to DataStore", e)
        }
    }

    val personalityFlow: Flow<BotPersonality> = context.dataStore.data
        .catch { exception ->
            android.util.Log.e("PersonalityDataStore", "Error reading personality DataStore", exception)
            emit(emptyPreferences())
        }
        .map { prefs ->
            BotPersonality(
                id = prefs[Keys.ID] ?: "default_persona",
                name = prefs[Keys.NAME] ?: "Galaxy Guardian Alpha",
                tone = prefs[Keys.TONE] ?: "Vigilant, protective, and mission-critical",
                traits = prefs[Keys.TRAITS] ?: "Analytical, defensive, highly disciplined, security-conscious",
                systemInstructions = prefs[Keys.SYSTEM_INSTRUCTIONS] ?: "Always validate all inputs before processing. Never output plain text secrets or passwords. Write clean PEP 8 code with comprehensive docstrings and type annotations. Log warnings for anomalous events.",
                negativeConstraints = prefs[Keys.NEGATIVE_CONSTRAINTS] ?: "Do NOT use eval(), exec(), shell=True, or wildcard imports. Do NOT leave untyped functions.",
                responseStyle = prefs[Keys.RESPONSE_STYLE] ?: "Modular enterprise Python with clear terminal feedback",
                creativityTemperature = prefs[Keys.CREATIVITY_TEMP] ?: 0.3f,
                avatarType = prefs[Keys.AVATAR_TYPE] ?: "icon",
                avatarValue = prefs[Keys.AVATAR_VALUE] ?: "shield",
                avatarColorIndex = prefs[Keys.AVATAR_COLOR_INDEX] ?: 0
            )
        }

    suspend fun savePersonality(personality: BotPersonality) {
        try {
            context.dataStore.edit { prefs ->
                prefs[Keys.ID] = personality.id
                prefs[Keys.NAME] = personality.name
                prefs[Keys.TONE] = personality.tone
                prefs[Keys.TRAITS] = personality.traits
                prefs[Keys.SYSTEM_INSTRUCTIONS] = personality.systemInstructions
                prefs[Keys.NEGATIVE_CONSTRAINTS] = personality.negativeConstraints
                prefs[Keys.RESPONSE_STYLE] = personality.responseStyle
                prefs[Keys.CREATIVITY_TEMP] = personality.creativityTemperature
                prefs[Keys.AVATAR_TYPE] = personality.avatarType
                prefs[Keys.AVATAR_VALUE] = personality.avatarValue
                prefs[Keys.AVATAR_COLOR_INDEX] = personality.avatarColorIndex
            }
        } catch (e: Throwable) {
            android.util.Log.e("PersonalityDataStore", "Error writing personality to DataStore", e)
        }
    }

    suspend fun clearPersonality() {
        try {
            context.dataStore.edit { prefs ->
                prefs.clear()
            }
        } catch (e: Throwable) {
            android.util.Log.e("PersonalityDataStore", "Error clearing DataStore", e)
        }
    }
}
