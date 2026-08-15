package com.example.weathersphere.data.repository

import com.example.weathersphere.data.api.RetrofitInstance
import com.example.weathersphere.data.local.FavoriteCity
import com.example.weathersphere.data.local.FavoriteCityDao
import com.example.weathersphere.data.model.CitySuggestion
import com.example.weathersphere.data.model.HourlyForecastResponse
import com.example.weathersphere.data.model.WeatherResponse
import com.example.weathersphere.data.model.WeeklyForecastResponse

class WeatherRepository(
    private val favoriteCityDao: FavoriteCityDao
) {

    suspend fun getCurrentWeather(
        city: String
    ): WeatherResponse {

        return RetrofitInstance.api.getCurrentWeather(city)
    }

    suspend fun getHourlyForecast(
        city: String
    ): HourlyForecastResponse {

        return RetrofitInstance.api.getHourlyForecast(city)
    }

    suspend fun getWeeklyForecast(
        city: String
    ): WeeklyForecastResponse {

        return RetrofitInstance.api.getWeeklyForecast(city)
    }

    suspend fun getWeatherByLocation(
        latitude: Double,
        longitude: Double
    ): WeatherResponse {

        return RetrofitInstance.api.getWeatherByLocation(
            latitude,
            longitude
        )
    }

    suspend fun insertFavorite(
        city: FavoriteCity
    ) {
        favoriteCityDao.insertCity(city)
    }

    suspend fun deleteFavorite(
        city: FavoriteCity
    ) {
        favoriteCityDao.deleteCity(city)
    }

    suspend fun getFavorites(): List<FavoriteCity> {
        return favoriteCityDao.getAllCities()
    }

    suspend fun searchCities(
        query: String
    ): List<CitySuggestion> {

        return RetrofitInstance.api.searchCities(query)

    }
}