package com.example.weathersphere.data.model

data class WeeklyForecastResponse(
    val forecast: WeeklyForecast
)

data class WeeklyForecast(
    val forecastday: List<WeekDay>
)

data class WeekDay(
    val date: String,
    val day: Day
)

data class Day(
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val condition: Condition
)