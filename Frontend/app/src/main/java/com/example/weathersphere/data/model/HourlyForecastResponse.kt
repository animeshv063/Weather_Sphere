package com.example.weathersphere.data.model

data class HourlyForecastResponse(
    val forecast: Forecast
)

data class Forecast(
    val forecastday : List<ForecastDay>
)

data class ForecastDay(
    val hour : List<Hour>
)

data class Hour(
    val time : String,
    val temp_c : Double,
    val humidity : Int,
    val condition : Condition
)

data class Condition(
    val text : String,
    val icon : String
)