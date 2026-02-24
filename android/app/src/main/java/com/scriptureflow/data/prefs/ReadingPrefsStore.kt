package com.scriptureflow.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.scriptureflow.core.model.ReadingPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.readingPrefsDataStore by preferencesDataStore(name = "reading_preferences")

/**
 * DataStore Preferences wrapper for ReadingPreferences.
 *
 * Contract fields:
 * - font_size_sp (float)
 * - line_height_multiplier (float)
 * - font_style ("serif" | "sans")
 * - theme_mode ("light" | "dark" | "system")
 * - text_alignment ("start" | "center" | "justify")
 */
class ReadingPrefsStore(
    private val context: Context,
) {

    val defaultPreferences: ReadingPreferences = ReadingPreferences(
        fontSizeSp = 18f,
        lineHeightMultiplier = 1.3f,
        fontStyle = FONT_STYLE_SERIF,
        themeMode = THEME_MODE_SYSTEM,
        textAlignment = TEXT_ALIGN_START,
    )

    val preferencesFlow: Flow<ReadingPreferences> = context.readingPrefsDataStore.data
        .map { prefs ->
            ReadingPreferences(
                fontSizeSp = prefs[Keys.fontSizeSp] ?: defaultPreferences.fontSizeSp,
                lineHeightMultiplier = prefs[Keys.lineHeightMultiplier]
                    ?: defaultPreferences.lineHeightMultiplier,
                fontStyle = sanitizeFontStyle(prefs[Keys.fontStyle] ?: defaultPreferences.fontStyle),
                themeMode = sanitizeThemeMode(prefs[Keys.themeMode] ?: defaultPreferences.themeMode),
                textAlignment = sanitizeTextAlignment(
                    prefs[Keys.textAlignment] ?: defaultPreferences.textAlignment,
                ),
            )
        }

    suspend fun updateFontSizeSp(value: Float) {
        context.readingPrefsDataStore.edit { it[Keys.fontSizeSp] = value.coerceIn(12f, 42f) }
    }

    suspend fun updateLineHeightMultiplier(value: Float) {
        context.readingPrefsDataStore.edit { it[Keys.lineHeightMultiplier] = value.coerceIn(1.0f, 2.0f) }
    }

    suspend fun updateFontStyle(value: String) {
        context.readingPrefsDataStore.edit { it[Keys.fontStyle] = sanitizeFontStyle(value) }
    }

    suspend fun updateThemeMode(value: String) {
        context.readingPrefsDataStore.edit { it[Keys.themeMode] = sanitizeThemeMode(value) }
    }

    suspend fun updateTextAlignment(value: String) {
        context.readingPrefsDataStore.edit { it[Keys.textAlignment] = sanitizeTextAlignment(value) }
    }

    suspend fun resetToDefaults() {
        context.readingPrefsDataStore.edit { prefs ->
            prefs[Keys.fontSizeSp] = defaultPreferences.fontSizeSp
            prefs[Keys.lineHeightMultiplier] = defaultPreferences.lineHeightMultiplier
            prefs[Keys.fontStyle] = defaultPreferences.fontStyle
            prefs[Keys.themeMode] = defaultPreferences.themeMode
            prefs[Keys.textAlignment] = defaultPreferences.textAlignment
        }
    }

    private fun sanitizeFontStyle(value: String): String {
        return when (value) {
            FONT_STYLE_SERIF, FONT_STYLE_SANS -> value
            else -> defaultPreferences.fontStyle
        }
    }

    private fun sanitizeThemeMode(value: String): String {
        return when (value) {
            THEME_MODE_LIGHT, THEME_MODE_DARK, THEME_MODE_SYSTEM -> value
            else -> defaultPreferences.themeMode
        }
    }

    private fun sanitizeTextAlignment(value: String): String {
        return when (value) {
            TEXT_ALIGN_START, TEXT_ALIGN_CENTER, TEXT_ALIGN_JUSTIFY -> value
            else -> defaultPreferences.textAlignment
        }
    }

    private object Keys {
        val fontSizeSp = floatPreferencesKey("font_size_sp")
        val lineHeightMultiplier = floatPreferencesKey("line_height_multiplier")
        val fontStyle = stringPreferencesKey("font_style")
        val themeMode = stringPreferencesKey("theme_mode")
        val textAlignment = stringPreferencesKey("text_alignment")
    }

    companion object {
        const val FONT_STYLE_SERIF: String = "serif"
        const val FONT_STYLE_SANS: String = "sans"

        const val THEME_MODE_LIGHT: String = "light"
        const val THEME_MODE_DARK: String = "dark"
        const val THEME_MODE_SYSTEM: String = "system"

        const val TEXT_ALIGN_START: String = "start"
        const val TEXT_ALIGN_CENTER: String = "center"
        const val TEXT_ALIGN_JUSTIFY: String = "justify"
    }
}
