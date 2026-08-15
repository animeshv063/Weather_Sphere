package com.example.weathersphere.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersphere.data.local.AppDatabase
import com.example.weathersphere.data.local.FavoriteCity
import com.example.weathersphere.data.repository.WeatherRepository
import com.example.weathersphere.datastore.SettingsDataStore
import com.example.weathersphere.notification.WeatherNotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeatherViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = WeatherRepository(
        AppDatabase.getDatabase(getApplication()).favoriteCityDao()
    )

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState

    init {
        loadFavorites()
        loadSettings()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            val favorites = repository.getFavorites()
            _uiState.value = _uiState.value.copy(favorites = favorites)
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val isCelsius = SettingsDataStore.getTemperatureUnit(context).first()
            val isCanvasAnim = SettingsDataStore.getCanvasAnimationEnabled(context).first()
            val isBriefing = SettingsDataStore.getDailyBriefingEnabled(context).first()
            val isAlerts = SettingsDataStore.getSevereAlertsEnabled(context).first()
            val briefingTime = SettingsDataStore.getDailyBriefingTime(context).first()

            _uiState.value = _uiState.value.copy(
                isCelsius = isCelsius,
                isCanvasAnimationEnabled = isCanvasAnim,
                isDailyBriefingEnabled = isBriefing,
                isSevereAlertsEnabled = isAlerts,
                dailyBriefingTime = briefingTime
            )
        }
    }

    fun changeDailyBriefingTime(time: String) {
        _uiState.value = _uiState.value.copy(dailyBriefingTime = time)
        viewModelScope.launch {
            SettingsDataStore.saveDailyBriefingTime(getApplication(), time)
        }
    }

    fun searchCity(city: String) {
        if (city.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val weather = repository.getCurrentWeather(city)
                val hourly = repository.getHourlyForecast(city)
                val weekly = repository.getWeeklyForecast(city)
                val favorites = repository.getFavorites()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    weather = weather,
                    hourlyForecast = hourly.forecast.forecastday.first().hour,
                    weeklyForecast = weekly.forecast.forecastday,
                    favorites = favorites,
                    suggestions = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to fetch weather data."
                )
            }
        }
    }

    fun saveFavorite(city: String) {
        if (city.isBlank()) return
        viewModelScope.launch {
            val currentFavs = repository.getFavorites()
            val exists = currentFavs.any { it.city.equals(city, ignoreCase = true) }
            if (!exists) {
                repository.insertFavorite(FavoriteCity(city = city.trim()))
            }
            val updatedFavs = repository.getFavorites()
            _uiState.value = _uiState.value.copy(favorites = updatedFavs)
        }
    }

    fun deleteFavorite(city: FavoriteCity) {
        viewModelScope.launch {
            repository.deleteFavorite(city)
            val updatedFavs = repository.getFavorites()
            _uiState.value = _uiState.value.copy(favorites = updatedFavs)
        }
    }

    fun changeTemperatureUnit(isCelsius: Boolean) {
        _uiState.value = _uiState.value.copy(isCelsius = isCelsius)
        viewModelScope.launch {
            SettingsDataStore.saveTemperatureUnit(getApplication(), isCelsius)
        }
    }

    fun toggleCanvasAnimation(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isCanvasAnimationEnabled = enabled)
        viewModelScope.launch {
            SettingsDataStore.saveCanvasAnimationEnabled(getApplication(), enabled)
        }
    }

    fun toggleDailyBriefing(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDailyBriefingEnabled = enabled)
        viewModelScope.launch {
            val context = getApplication<Application>()
            SettingsDataStore.saveDailyBriefingEnabled(context, enabled)
            if (enabled) {
                val currentCity = _uiState.value.weather?.location?.name ?: "Tokyo"
                val currentTemp = _uiState.value.weather?.current?.temp_c?.let { "${it.toInt()}°C" } ?: "24°C"
                val currentCondition = _uiState.value.weather?.current?.condition?.text ?: "Clear Sky"
                WeatherNotificationHelper.sendDailyBriefingNotification(
                    context = context,
                    cityName = currentCity,
                    temp = currentTemp,
                    condition = currentCondition
                )
            }
        }
    }

    fun toggleSevereAlerts(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isSevereAlertsEnabled = enabled)
        viewModelScope.launch {
            val context = getApplication<Application>()
            SettingsDataStore.saveSevereAlertsEnabled(context, enabled)
            if (enabled) {
                WeatherNotificationHelper.sendSevereAlertNotification(
                    context = context,
                    title = "⚠️ Weather Alerts Activated",
                    message = "You will now receive severe weather sound notifications."
                )
            }
        }
    }

    fun triggerTestBriefing() {
        val context = getApplication<Application>()
        val currentCity = _uiState.value.weather?.location?.name ?: "Tokyo"
        val currentTemp = _uiState.value.weather?.current?.temp_c?.let { "${it.toInt()}°C" } ?: "24°C"
        val currentCondition = _uiState.value.weather?.current?.condition?.text ?: "Sunny"
        WeatherNotificationHelper.sendDailyBriefingNotification(
            context = context,
            cityName = currentCity,
            temp = currentTemp,
            condition = currentCondition
        )
    }

    fun searchSuggestions(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(suggestions = emptyList())
            return
        }

        viewModelScope.launch {
            try {
                val results = repository.searchCities(query)
                _uiState.value = _uiState.value.copy(suggestions = results)
            } catch (_: Exception) {
                // Ignore autocomplete failures silently
            }
        }
    }
}