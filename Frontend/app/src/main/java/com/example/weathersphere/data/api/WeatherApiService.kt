package com.example.weathersphere.data.api

import com.example.weathersphere.data.model.CitySuggestion
import com.example.weathersphere.data.model.HourlyForecastResponse
import com.example.weathersphere.data.model.WeatherResponse
import com.example.weathersphere.data.model.WeeklyForecastResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather/current")
    suspend fun getCurrentWeather(
        @Query("city") city: String
    ): WeatherResponse

    @GET("weather/hourly/{city}")
    suspend fun getHourlyForecast(
        @Path("city") city: String
    ): HourlyForecastResponse

    @GET("weather/weekly/{city}")
    suspend fun getWeeklyForecast(
        @Path("city") city: String
    ): WeeklyForecastResponse

    @GET("weather/location")
    suspend fun getWeatherByLocation(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): WeatherResponse

    @GET("weather/search")
    suspend fun searchCities(
        @Query("query") query: String
    ): List<CitySuggestion>
}