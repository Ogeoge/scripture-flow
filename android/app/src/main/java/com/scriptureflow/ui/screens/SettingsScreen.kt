package com.scriptureflow.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scriptureflow.core.model.ReadingPreferences
import com.scriptureflow.data.prefs.ReadingPrefsStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    readingPrefsStore: ReadingPrefsStore,
    preferences: ReadingPreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
    ) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Reading",
                style = MaterialTheme.typography.titleMedium,
            )

            SettingSlider(
                title = "Font size",
                valueLabel = "${preferences.fontSizeSp.toInt()} sp",
                value = preferences.fontSizeSp,
                valueRange = 12f..42f,
                steps = (42 - 12) - 1,
                onValueChange = { v ->
                    scope.launch { readingPrefsStore.updateFontSizeSp(v) }
                },
            )

            SettingSlider(
                title = "Line spacing",
                valueLabel = String.format("%.1f×", preferences.lineHeightMultiplier),
                value = preferences.lineHeightMultiplier,
                valueRange = 1.0f..2.0f,
                steps = 10 - 1,
                onValueChange = { v ->
                    scope.launch { readingPrefsStore.updateLineHeightMultiplier(v) }
                },
            )

            Divider()

            Text(
                text = "Font style",
                style = MaterialTheme.typography.titleSmall,
            )
            RadioGroupRow(
                options = listOf(
                    "Serif" to ReadingPrefsStore.FONT_STYLE_SERIF,
                    "Sans" to ReadingPrefsStore.FONT_STYLE_SANS,
                ),
                selected = preferences.fontStyle,
                onSelected = { value ->
                    scope.launch { readingPrefsStore.updateFontStyle(value) }
                },
            )

            Divider()

            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
            )
            RadioGroupColumn(
                options = listOf(
                    "System" to ReadingPrefsStore.THEME_MODE_SYSTEM,
                    "Light" to ReadingPrefsStore.THEME_MODE_LIGHT,
                    "Dark" to ReadingPrefsStore.THEME_MODE_DARK,
                ),
                selected = preferences.themeMode,
                onSelected = { value ->
                    scope.launch { readingPrefsStore.updateThemeMode(value) }
                },
            )

            Divider()

            Text(
                text = "Text alignment",
                style = MaterialTheme.typography.titleSmall,
            )
            RadioGroupRow(
                options = listOf(
                    "Start" to ReadingPrefsStore.TEXT_ALIGN_START,
                    "Center" to ReadingPrefsStore.TEXT_ALIGN_CENTER,
                    "Justify" to ReadingPrefsStore.TEXT_ALIGN_JUSTIFY,
                ),
                selected = preferences.textAlignment,
                onSelected = { value ->
                    scope.launch { readingPrefsStore.updateTextAlignment(value) }
                },
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch { readingPrefsStore.resetToDefaults() }
                    },
                ) {
                    Text("Reset")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onBack,
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(valueLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps.coerceAtLeast(0),
        )
    }
}

@Composable
private fun RadioGroupRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for ((label, value) in options) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected == value,
                    onClick = { onSelected(value) },
                )
                Text(label)
            }
        }
    }
}

@Composable
private fun RadioGroupColumn(
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for ((label, value) in options) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selected == value,
                    onClick = { onSelected(value) },
                )
                Text(label)
            }
        }
    }
}
