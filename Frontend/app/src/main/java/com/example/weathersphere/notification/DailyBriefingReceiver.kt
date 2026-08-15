package com.example.weathersphere.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.weathersphere.data.local.AppDatabase
import com.example.weathersphere.data.repository.WeatherRepository
import com.example.weathersphere.datastore.SettingsDataStore
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class DailyBriefingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isEnabled = SettingsDataStore.getDailyBriefingEnabled(context).first()
                if (isEnabled) {
                    val repository = WeatherRepository(
                        AppDatabase.getDatabase(context).favoriteCityDao()
                    )

                    val hasLocationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    val location: Location? = if (hasLocationPermission) {
                        try {
                            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                            suspendCancellableCoroutine { continuation ->
                                fusedClient.lastLocation
                                    .addOnSuccessListener { loc ->
                                        if (continuation.isActive) {
                                            continuation.resume(loc)
                                        }
                                    }
                                    .addOnFailureListener {
                                        if (continuation.isActive) {
                                            continuation.resume(null)
                                        }
                                    }
                            }
                        } catch (_: Exception) {
                            null
                        }
                    } else {
                        null
                    }

                    val weather = try {
                        if (location != null) {
                            repository.getWeatherByLocation(location.latitude, location.longitude)
                        } else {
                            val favorites = try { repository.getFavorites() } catch (_: Exception) { emptyList() }
                            val targetCity = favorites.firstOrNull()?.city ?: "Tokyo"
                            repository.getCurrentWeather(targetCity)
                        }
                    } catch (_: Exception) {
                        null
                    }

                    val cityName = weather?.location?.name ?: "Your Area"
                    val temp = weather?.current?.temp_c?.let { "${it.toInt()}°C" } ?: "24°C"
                    val condition = weather?.current?.condition?.text ?: "Clear Sky"

                    WeatherNotificationHelper.sendDailyBriefingNotification(
                        context = context,
                        cityName = cityName,
                        temp = temp,
                        condition = condition
                    )

                    // Reschedule for next day at the user's configured time
                    val savedTime = SettingsDataStore.getDailyBriefingTime(context).first()
                    WeatherNotificationHelper.scheduleDailyBriefing(context, savedTime)
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
