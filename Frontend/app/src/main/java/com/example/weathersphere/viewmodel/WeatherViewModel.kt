package com.example.weathersphere.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersphere.data.local.AppDatabase
import com.example.weathersphere.data.local.FavoriteCity
import com.example.weathersphere.data.repository.WeatherRepository
import com.example.weathersphere.datastore.SettingsDataStore
import com.example.weathersphere.location.LocationHelper
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

            if (isBriefing) {
                WeatherNotificationHelper.scheduleDailyBriefing(context, briefingTime)
            }

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
            val context = getApplication<Application>()
            SettingsDataStore.saveDailyBriefingTime(context, time)
            if (_uiState.value.isDailyBriefingEnabled) {
                WeatherNotificationHelper.scheduleDailyBriefing(context, time)
            }
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
                    hourlyForecast = hourly.forecast.forecastday.firstOrNull()?.hour ?: emptyList(),
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
                val briefingTime = _uiState.value.dailyBriefingTime
                WeatherNotificationHelper.scheduleDailyBriefing(context, briefingTime)
                fetchAndSendDailyBriefing(context)
            } else {
                WeatherNotificationHelper.cancelDailyBriefing(context)
            }
        }
    }

    fun sendDailyBriefingNow() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            fetchAndSendDailyBriefing(context)
        }
    }

    private suspend fun fetchAndSendDailyBriefing(context: Application) {
        val location = LocationHelper.getDeviceLocation(context)
        if (location != null) {
            try {
                val weather = repository.getWeatherByLocation(location.latitude, location.longitude)
                val tempStr = if (_uiState.value.isCelsius) {
                    "${weather.current.temp_c.toInt()}°C"
                } else {
                    "${((weather.current.temp_c * 9.0 / 5.0) + 32.0).toInt()}°F"
                }
                WeatherNotificationHelper.sendDailyBriefingNotification(
                    context = context,
                    cityName = weather.location.name,
                    temp = tempStr,
                    condition = weather.current.condition.text
                )
            } catch (_: Exception) {
                WeatherNotificationHelper.sendDailyBriefingNotification(
                    context = context,
                    cityName = "Your Device Location",
                    temp = "--",
                    condition = "Scheduled for ${_uiState.value.dailyBriefingTime}"
                )
            }
        } else {
            WeatherNotificationHelper.sendDailyBriefingNotification(
                context = context,
                cityName = "Device Location",
                temp = "--",
                condition = "Scheduled for ${_uiState.value.dailyBriefingTime}. Enable GPS permissions for local briefing."
            )
        }
    }

    fun toggleSevereAlerts(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isSevereAlertsEnabled = enabled)
        viewModelScope.launch {
            val context = getApplication<Application>()
            SettingsDataStore.saveSevereAlertsEnabled(context, enabled)
            if (enabled) {
                fetchAndSendSevereAlert(context)
            }
        }
    }

    fun sendSevereAlertsNow() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            fetchAndSendSevereAlert(context)
        }
    }

    private suspend fun fetchAndSendSevereAlert(context: Application) {
        val location = LocationHelper.getDeviceLocation(context)
        if (location != null) {
            try {
                val weather = repository.getWeatherByLocation(location.latitude, location.longitude)
                val cityName = weather.location.name
                val conditionText = weather.current.condition.text
                val windKph = weather.current.wind_kph
                val tempC = weather.current.temp_c
                val tempStr = if (_uiState.value.isCelsius) "${tempC.toInt()}°C" else "${((tempC * 9.0 / 5.0) + 32.0).toInt()}°F"

                val lowerCondition = conditionText.lowercase()
                val isSevereCondition = lowerCondition.contains("thunder") ||
                        lowerCondition.contains("storm") ||
                        lowerCondition.contains("heavy") ||
                        lowerCondition.contains("blizzard") ||
                        lowerCondition.contains("tornado") ||
                        lowerCondition.contains("cyclone") ||
                        lowerCondition.contains("hurricane") ||
                        lowerCondition.contains("gale") ||
                        lowerCondition.contains("squall") ||
                        lowerCondition.contains("hail") ||
                        lowerCondition.contains("flood") ||
                        windKph >= 55.0 ||
                        tempC >= 42.0 ||
                        tempC <= -15.0

                if (isSevereCondition) {
                    WeatherNotificationHelper.sendSevereAlertNotification(
                        context = context,
                        title = "⚠️ Severe Weather Warning • $cityName",
                        message = "Warning: $conditionText, $tempStr with wind ${windKph.toInt()} km/h. Please stay indoors and take safety precautions."
                    )
                } else {
                    WeatherNotificationHelper.sendSevereAlertNotification(
                        context = context,
                        title = "🛡️ Weather Alerts Active • $cityName",
                        message = "Current condition: $tempStr, $conditionText. Actively monitoring $cityName for severe weather hazards."
                    )
                }
            } catch (_: Exception) {
                WeatherNotificationHelper.sendSevereAlertNotification(
                    context = context,
                    title = "🛡️ Severe Weather Alerts Active",
                    message = "Active monitoring enabled for extreme weather events."
                )
            }
        } else {
            WeatherNotificationHelper.sendSevereAlertNotification(
                context = context,
                title = "🛡️ Severe Weather Alerts Active",
                message = "Active monitoring enabled. Please ensure location permissions are granted for your device's coordinates."
            )
        }
    }

    fun loadWeatherByCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val context = getApplication<Application>()
            val location = LocationHelper.getDeviceLocation(context)
            if (location != null) {
                try {
                    val weather = repository.getWeatherByLocation(location.latitude, location.longitude)
                    val hourly = try { repository.getHourlyForecast(weather.location.name) } catch (_: Exception) { null }
                    val weekly = try { repository.getWeeklyForecast(weather.location.name) } catch (_: Exception) { null }
                    val favorites = repository.getFavorites()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        weather = weather,
                        hourlyForecast = hourly?.forecast?.forecastday?.firstOrNull()?.hour ?: emptyList(),
                        weeklyForecast = weekly?.forecast?.forecastday ?: emptyList(),
                        favorites = favorites,
                        suggestions = emptyList()
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to fetch weather for your location."
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Could not retrieve device location. Please enable GPS and location permissions."
                )
            }
        }
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