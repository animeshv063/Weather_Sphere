package com.example.weathersphere.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersphere.data.model.Hour
import com.example.weathersphere.ui.components.GlassCard

@Composable
fun HourlyForecast(
    hours: List<Hour>,
    isCelsius: Boolean = true
) {
    if (hours.isEmpty()) {
        Text(
            "No hourly forecast available",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(hours) { hour ->
            val formattedTime = rememberFormattedTime(hour.time)
            val displayTemp = if (isCelsius) "${hour.temp_c}°C" else "${(hour.temp_c * 9/5 + 32).toInt()}°F"

            GlassCard(
                modifier = Modifier.width(104.dp),
                elevation = 4.dp,
                contentPadding = 16.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    WeatherIcon(
                        iconUrl = hour.condition.icon,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = displayTemp,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = hour.condition.text,
                        fontSize = 11.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberFormattedTime(rawTime: String): String {
    return try {
        if (rawTime.contains(" ")) {
            rawTime.split(" ")[1]
        } else {
            rawTime
        }
    } catch (_: Exception) {
        rawTime
    }
}