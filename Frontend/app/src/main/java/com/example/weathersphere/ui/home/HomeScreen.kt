package com.example.weathersphere.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersphere.ui.components.GlassCard
import com.example.weathersphere.ui.components.WeatherCard
import com.example.weathersphere.viewmodel.WeatherUiState

@Composable
fun HomeScreen(
    uiState: WeatherUiState,
    onSearch: (String) -> Unit,
    onTyping: (String) -> Unit,
    onAddFavorite: (String) -> Unit
) {
    var cityInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.weather?.location?.name) {
        uiState.weather?.location?.name?.let {
            if (it.isNotBlank()) {
                cityInput = it
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. SEARCH BAR & AUTOCOMPLETE ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                contentPadding = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = cityInput,
                        onValueChange = {
                            cityInput = it
                            onTyping(it)
                        },
                        placeholder = { Text("Search city (e.g. Tokyo, Paris)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    if (cityInput.isNotEmpty()) {
                        IconButton(onClick = {
                            cityInput = ""
                            onTyping("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }

                    IconButton(
                        onClick = {
                            if (cityInput.isNotBlank()) onSearch(cityInput)
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Submit Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Suggestions Dropdown Card
        if (uiState.suggestions.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = uiState.suggestions.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 8.dp,
                        contentPadding = 12.dp
                    ) {
                        Column {
                            uiState.suggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            cityInput = suggestion.name
                                            onSearch(suggestion.name)
                                        }
                                        .padding(vertical = 12.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = suggestion.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "${suggestion.region}, ${suggestion.country}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }
        }

        // --- 2. MAIN CONTENT AREA ---
        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "Fetching weather telemetry...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            uiState.error != null -> {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0x33EF4444)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.error,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            uiState.weather != null -> {
                val weather = uiState.weather
                val isCelsius = uiState.isCelsius
                val displayTemp = if (isCelsius) "${weather.current.temp_c}°C" else "${(weather.current.temp_c * 9/5 + 32).toInt()}°F"
                val feelsLikeTemp = if (isCelsius) "${weather.current.feelslike_c}°C" else "${(weather.current.feelslike_c * 9/5 + 32).toInt()}°F"

                val isFav = uiState.favorites.any { it.city.equals(weather.location.name, ignoreCase = true) }

                // --- HERO WEATHER CARD ---
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 10.dp,
                        shape = RoundedCornerShape(32.dp),
                        contentPadding = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = weather.location.name,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = weather.location.country,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        onAddFavorite(weather.location.name)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) Color(0xFFF43F5E) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                WeatherIcon(
                                    iconUrl = weather.current.condition.icon,
                                    modifier = Modifier.size(100.dp)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = displayTemp,
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = weather.current.condition.text,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Feels like $feelsLikeTemp",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // SMART WEATHER TIP
                item {
                    SmartWeatherTip(conditionText = weather.current.condition.text, uvIndex = weather.current.uv)
                }

                // WEATHER STATS METRICS GRID
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Weather Conditions",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            WeatherCard(
                                title = "Humidity",
                                value = "${weather.current.humidity}%",
                                icon = Icons.Default.WaterDrop,
                                iconTint = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f)
                            )
                            WeatherCard(
                                title = "Wind",
                                value = "${weather.current.wind_kph} km/h",
                                icon = Icons.Default.Air,
                                iconTint = Color(0xFF8B5CF6),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            WeatherCard(
                                title = "Pressure",
                                value = "${weather.current.pressure_mb} mb",
                                icon = Icons.Default.Speed,
                                iconTint = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                            WeatherCard(
                                title = "UV Index",
                                value = weather.current.uv.toString(),
                                icon = Icons.Default.WbSunny,
                                iconTint = Color(0xFFF97316),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // HOURLY FORECAST SECTION
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Hourly Forecast",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                        HourlyForecast(hours = uiState.hourlyForecast, isCelsius = isCelsius)
                    }
                }

                // WEEKLY FORECAST SECTION
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "7-Day Forecast",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                        WeeklyForecast(forecast = uiState.weeklyForecast, isCelsius = isCelsius)
                    }
                }
            }

            else -> {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        contentPadding = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Search Weather",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(68.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Discover Atmospheric Weather",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Search any city or select a popular location below",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "POPULAR CITIES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val popularCities = listOf("Tokyo", "London", "New York", "Paris", "Sydney")
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                popularCities.forEach { city ->
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        modifier = Modifier.clickable {
                                            cityInput = city
                                            onSearch(city)
                                        }
                                    ) {
                                        Text(
                                            text = city,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
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
private fun SmartWeatherTip(conditionText: String, uvIndex: Double) {
    val (tipText, tipIcon, tipColor) = remember(conditionText, uvIndex) {
        val lower = conditionText.lowercase()
        when {
            lower.contains("rain") || lower.contains("drizzle") ->
                Triple("Pack an umbrella! Wet roads and showers expected today.", Icons.Default.Warning, Color(0xFF38BDF8))
            lower.contains("snow") || lower.contains("ice") ->
                Triple("Bundle up warmly! Freezing temperatures outside.", Icons.Default.AcUnit, Color(0xFF818CF8))
            uvIndex > 6 ->
                Triple("High UV index! Don't forget sunscreen and sunglasses.", Icons.Default.WbSunny, Color(0xFFF97316))
            lower.contains("sun") || lower.contains("clear") ->
                Triple("Beautiful clear weather! Great time for outdoor activities.", Icons.Default.CheckCircle, Color(0xFF10B981))
            else ->
                Triple("Pleasant weather conditions expected throughout the day.", Icons.Default.Info, Color(0xFFF59E0B))
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = tipColor.copy(alpha = 0.15f),
        borderColor = tipColor.copy(alpha = 0.4f),
        contentPadding = 18.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = tipIcon,
                contentDescription = "Tip",
                tint = tipColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = tipText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}