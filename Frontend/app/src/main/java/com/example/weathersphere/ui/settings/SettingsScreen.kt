package com.example.weathersphere.ui.settings

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersphere.ui.components.GlassCard
import java.util.Locale

@Composable
fun SettingsScreen(
    isCelsius: Boolean,
    isCanvasAnimationEnabled: Boolean,
    isDailyBriefingEnabled: Boolean,
    isSevereAlertsEnabled: Boolean,
    dailyBriefingTime: String = "07:00 AM",
    onUnitChanged: (Boolean) -> Unit,
    onToggleCanvasAnimation: (Boolean) -> Unit,
    onToggleDailyBriefing: (Boolean) -> Unit,
    onToggleSevereAlerts: (Boolean) -> Unit,
    onReminderTimeChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var windUnit by remember { mutableStateOf("km/h") }
    var cacheClearedMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HERO HEADER ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                contentPadding = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Preferences",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customize WeatherSphere app options",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // --- SECTION 1: UNITS & MEASUREMENTS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsSectionTitle(title = "UNITS & MEASUREMENT", icon = Icons.Default.Info)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Temperature Unit",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isCelsius) "Celsius (°C)" else "Fahrenheit (°F)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Row {
                                FilterChip(
                                    selected = isCelsius,
                                    onClick = { onUnitChanged(true) },
                                    label = { Text("°C") },
                                    shape = CircleShape
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = !isCelsius,
                                    onClick = { onUnitChanged(false) },
                                    label = { Text("°F") },
                                    shape = CircleShape
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.padding(vertical = 14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Wind Speed Unit",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Display wind velocity in $windUnit",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Row {
                                FilterChip(
                                    selected = windUnit == "km/h",
                                    onClick = { windUnit = "km/h" },
                                    label = { Text("km/h") },
                                    shape = CircleShape
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = windUnit == "mph",
                                    onClick = { windUnit = "mph" },
                                    label = { Text("mph") },
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: NOTIFICATIONS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsSectionTitle(title = "NOTIFICATIONS & ALERTS", icon = Icons.Default.Notifications)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Severe Weather Alerts",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Push warnings for extreme weather events",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = isSevereAlertsEnabled,
                                onCheckedChange = { onToggleSevereAlerts(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF06B6D4)
                                )
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier.padding(vertical = 14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Daily Morning Briefing",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Get daily forecast summary every morning at $dailyBriefingTime",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = isDailyBriefingEnabled,
                                onCheckedChange = { onToggleDailyBriefing(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF06B6D4)
                                )
                            )
                        }

                        // Time Picker Selection Panel
                        AnimatedVisibility(visible = isDailyBriefingEnabled) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Reminder Time",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        ),
                                        modifier = Modifier.clickable {
                                            val parts = dailyBriefingTime.replace("AM", "").replace("PM", "").trim().split(":")
                                            val isPm = dailyBriefingTime.contains("PM", ignoreCase = true)
                                            var initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 7
                                            if (isPm && initialHour < 12) initialHour += 12
                                            if (!isPm && initialHour == 12) initialHour = 0
                                            val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                                            val dialog = TimePickerDialog(
                                                context,
                                                { _, hourOfDay, minute ->
                                                    val amPm = if (hourOfDay < 12) "AM" else "PM"
                                                    val displayHour = when {
                                                        hourOfDay == 0 -> 12
                                                        hourOfDay > 12 -> hourOfDay - 12
                                                        else -> hourOfDay
                                                    }
                                                    val formatted = String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
                                                    onReminderTimeChanged(formatted)
                                                },
                                                initialHour,
                                                initialMinute,
                                                false
                                            )
                                            dialog.show()
                                        }
                                    ) {
                                        Text(
                                            text = dailyBriefingTime,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Quick Presets:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val presetOptions = listOf(
                                        "06:00 AM" to "6 AM",
                                        "07:00 AM" to "7 AM",
                                        "08:00 AM" to "8 AM",
                                        "09:00 AM" to "9 AM"
                                    )
                                    presetOptions.forEach { (timeValue, displayLabel) ->
                                        val isSelected = dailyBriefingTime.equals(timeValue, ignoreCase = true)
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onReminderTimeChanged(timeValue) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 1.8.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = displayLabel,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        val parts = dailyBriefingTime.replace("AM", "").replace("PM", "").trim().split(":")
                                        val isPm = dailyBriefingTime.contains("PM", ignoreCase = true)
                                        var initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 7
                                        if (isPm && initialHour < 12) initialHour += 12
                                        if (!isPm && initialHour == 12) initialHour = 0
                                        val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                                        val dialog = TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                val amPm = if (hourOfDay < 12) "AM" else "PM"
                                                val displayHour = when {
                                                    hourOfDay == 0 -> 12
                                                    hourOfDay > 12 -> hourOfDay - 12
                                                    else -> hourOfDay
                                                }
                                                val formatted = String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
                                                onReminderTimeChanged(formatted)
                                            },
                                            initialHour,
                                            initialMinute,
                                            false
                                        )
                                        dialog.show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.2.dp,
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Set Custom Time...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 3: APPEARANCE & ANIMATIONS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsSectionTitle(title = "APPEARANCE & VISUALS", icon = Icons.Default.Palette)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Canvas Background Animation",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isCanvasAnimationEnabled) "Live 60FPS particle background animations active" else "Static atmospheric gradient backgrounds",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked = isCanvasAnimationEnabled,
                                onCheckedChange = { onToggleCanvasAnimation(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF06B6D4)
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 4: DATA & STORAGE ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsSectionTitle(title = "DATA MANAGEMENT", icon = Icons.Default.Refresh)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Clear Cached Weather Data",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Free local database cache and reset weather response state",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Button(
                                onClick = {
                                    cacheClearedMessage = "Local weather cache cleared successfully!"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF43F5E).copy(alpha = 0.2f),
                                    contentColor = Color(0xFFF43F5E)
                                ),
                                shape = CircleShape,
                                modifier = Modifier.height(44.dp)
                            ) {
                                Text("Clear Cache", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (cacheClearedMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = cacheClearedMessage!!,
                                fontSize = 13.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}