package com.example.weathersphere.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersphere.data.local.AppDatabase
import com.example.weathersphere.data.local.FavoriteCity
import com.example.weathersphere.data.repository.WeatherRepository
import com.example.weathersphere.datastore.SettingsDataStore
import com.example.weathersphere.notification.WeatherNotificationHelper
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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
                val briefingTime = _uiState.value.dailyBriefingTime
                WeatherNotificationHelper.scheduleDailyBriefing(context, briefingTime)

                val hasLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val weatherResponse = if (hasLocationPermission) {
                    try {
                        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                        val location = suspendCancellableCoroutine { continuation ->
                            fusedClient.lastLocation
                                .addOnSuccessListener { loc ->
                                    if (continuation.isActive) continuation.resume(loc)
                                }
                                .addOnFailureListener {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                        }
                        if (location != null) {
                            repository.getWeatherByLocation(location.latitude, location.longitude)
                        } else {
                            _uiState.value.weather ?: repository.getCurrentWeather("Tokyo")
                        }
                    } catch (_: Exception) {
                        _uiState.value.weather ?: repository.getCurrentWeather("Tokyo")
                    }
                } else {
                    _uiState.value.weather ?: repository.getCurrentWeather("Tokyo")
                }

                val currentCity = weatherResponse?.location?.name ?: "Your Area"
                val currentTemp = weatherResponse?.current?.temp_c?.let { "${it.toInt()}°C" } ?: "24°C"
                val currentCondition = weatherResponse?.current?.condition?.text ?: "Clear Sky"
                WeatherNotificationHelper.sendDailyBriefingNotification(
                    context = context,
                    cityName = currentCity,
                    temp = currentTemp,
                    condition = currentCondition
                )
            } else {
                WeatherNotificationHelper.cancelDailyBriefing(context)
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