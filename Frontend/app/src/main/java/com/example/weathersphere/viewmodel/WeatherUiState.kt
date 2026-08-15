package com.example.weathersphere.viewmodel

import com.example.weathersphere.data.local.FavoriteCity
import com.example.weathersphere.data.model.Hour
import com.example.weathersphere.data.model.WeatherResponse
import com.example.weathersphere.data.model.WeekDay
import com.example.weathersphere.data.model.CitySuggestion

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weather: WeatherResponse? = null,
    val hourlyForecast: List<Hour> = emptyList(),
    val weeklyForecast: List<WeekDay> = emptyList(),
    val favorites: List<FavoriteCity> = emptyList(),
    val isCelsius: Boolean = true,
    val isCanvasAnimationEnabled: Boolean = true,
    val isDailyBriefingEnabled: Boolean = false,
    val isSevereAlertsEnabled: Boolean = true,
    val dailyBriefingTime: String = "07:00 AM",
    val error: String? = null,
    val suggestions: List<CitySuggestion> = emptyList(),
)