package com.scriptureflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.scriptureflow.data.prefs.ReadingPrefsStore

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF2E6A5A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB2F0DA),
    onPrimaryContainer = Color(0xFF002019),

    secondary = Color(0xFF4B635B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE9DD),
    onSecondaryContainer = Color(0xFF082019),

    tertiary = Color(0xFF3F6375),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC3E8FD),
    onTertiaryContainer = Color(0xFF001E2A),

    background = Color(0xFFFBFDF9),
    onBackground = Color(0xFF191C1B),

    surface = Color(0xFFFBFDF9),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5DF),
    onSurfaceVariant = Color(0xFF3F4945),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFF6F7975),
    outlineVariant = Color(0xFFBFC9C3),
    scrim = Color(0xFF000000),
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF96D4BE),
    onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF145142),
    onPrimaryContainer = Color(0xFFB2F0DA),

    secondary = Color(0xFFB3CCC0),
    onSecondary = Color(0xFF1D352D),
    secondaryContainer = Color(0xFF344C43),
    onSecondaryContainer = Color(0xFFCEE9DD),

    tertiary = Color(0xFFA7CCE1),
    onTertiary = Color(0xFF0A3445),
    tertiaryContainer = Color(0xFF254B5C),
    onTertiaryContainer = Color(0xFFC3E8FD),

    background = Color(0xFF111413),
    onBackground = Color(0xFFE1E3E0),

    surface = Color(0xFF111413),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBFC9C3),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF89938E),
    outlineVariant = Color(0xFF3F4945),
    scrim = Color(0xFF000000),
)

private val AppTypography: Typography = Typography()

/**
 * Material 3 theme wrapper.
 *
 * themeMode uses contract-aligned values from [ReadingPrefsStore]:
 * - "light" | "dark" | "system"
 *
 * Dynamic color is enabled when available (Android 12+).
 */
@Composable
fun ScriptureFlowTheme(
    themeMode: String = ReadingPrefsStore.THEME_MODE_SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()

    val useDark = when (themeMode) {
        ReadingPrefsStore.THEME_MODE_LIGHT -> false
        ReadingPrefsStore.THEME_MODE_DARK -> true
        ReadingPrefsStore.THEME_MODE_SYSTEM -> systemDark
        else -> systemDark
    }

    val colorScheme = resolveColorScheme(useDark = useDark, dynamicColor = dynamicColor)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}

@Composable
private fun resolveColorScheme(
    useDark: Boolean,
    dynamicColor: Boolean,
): ColorScheme {
    val context = LocalContext.current

    return if (dynamicColor) {
        // dynamicDarkColorScheme/dynamicLightColorScheme internally guard on API level.
        if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (useDark) DarkColors else LightColors
    }
}
