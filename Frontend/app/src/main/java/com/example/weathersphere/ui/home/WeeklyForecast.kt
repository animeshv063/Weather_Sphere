package com.example.weathersphere.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersphere.data.model.WeekDay
import com.example.weathersphere.ui.components.GlassCard

@Composable
fun WeeklyForecast(
    forecast: List<WeekDay>,
    isCelsius: Boolean = true
) {
    if (forecast.isEmpty()) {
        Text(
            "No weekly forecast available",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        forecast.forEach { day ->
            val maxTemp = if (isCelsius) "${day.day.maxtemp_c}°" else "${(day.day.maxtemp_c * 9/5 + 32).toInt()}°"
            val minTemp = if (isCelsius) "${day.day.mintemp_c}°" else "${(day.day.mintemp_c * 9/5 + 32).toInt()}°"

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                contentPadding = 18.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = day.date,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = day.day.condition.text,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }

                    WeatherIcon(
                        iconUrl = day.day.condition.icon,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(horizontal = 4.dp)
                    )

                    Row(
                        modifier = Modifier.weight(1.5f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = minTemp,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Gradient range bar representation
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF38BDF8),
                                            Color(0xFFF59E0B)
                                        )
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = maxTemp,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}