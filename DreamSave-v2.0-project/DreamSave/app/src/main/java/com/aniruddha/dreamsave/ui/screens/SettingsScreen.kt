package com.aniruddha.dreamsave.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aniruddha.dreamsave.data.AppSettings
import com.aniruddha.dreamsave.data.ThemeMode
import com.aniruddha.dreamsave.ui.components.BrandBrush
import com.aniruddha.dreamsave.ui.components.GlassCard

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeMode: (ThemeMode) -> Unit,
    onCelebrations: (Boolean) -> Unit,
    onResetAll: () -> Unit
) {
    var showReset by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "title") {
            Column {
                Text(
                    text = "Make it yours",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge.copy(brush = BrandBrush)
                )
            }
        }

        item(key = "appearance") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Appearance", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Choose how DreamSave looks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.themeMode == ThemeMode.DARK,
                        onClick = { onThemeMode(ThemeMode.DARK) },
                        label = { Text("Dark") }
                    )
                    FilterChip(
                        selected = settings.themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeMode(ThemeMode.LIGHT) },
                        label = { Text("Light") }
                    )
                    FilterChip(
                        selected = settings.themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeMode(ThemeMode.SYSTEM) },
                        label = { Text("System") }
                    )
                }
            }
        }

        item(key = "celebrations") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Goal celebrations", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Show confetti when you reach a goal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = settings.celebrations, onCheckedChange = onCelebrations)
                }
            }
        }

        item(key = "data") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Your data", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Everything is stored privately on this device and is kept when you restart the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showReset = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete all goals and deposits", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        item(key = "about") {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("About", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("DreamSave v2.0", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "com.aniruddha.dreamsave",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Delete everything?") },
            text = { Text("All goals and deposits will be permanently removed. Your settings are kept.") },
            confirmButton = {
                TextButton(onClick = {
                    showReset = false
                    onResetAll()
                }) { Text("Delete all", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Cancel") }
            }
        )
    }
}
