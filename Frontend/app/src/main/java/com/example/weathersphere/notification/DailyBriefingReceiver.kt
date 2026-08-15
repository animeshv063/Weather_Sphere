package com.example.weathersphere.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weathersphere.data.local.AppDatabase
import com.example.weathersphere.data.repository.WeatherRepository
import com.example.weathersphere.datastore.SettingsDataStore
import com.example.weathersphere.location.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyBriefingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isEnabled = SettingsDataStore.getDailyBriefingEnabled(context).first()
                if (isEnabled) {
                    val isCelsius = SettingsDataStore.getTemperatureUnit(context).first()
                    val repository = WeatherRepository(
                        AppDatabase.getDatabase(context).favoriteCityDao()
                    )

                    val location = LocationHelper.getDeviceLocation(context)

                    if (location != null) {
                        try {
                            val weather = repository.getWeatherByLocation(location.latitude, location.longitude)
                            val cityName = weather.location.name
                            val temp = if (isCelsius) {
                                "${weather.current.temp_c.toInt()}°C"
                            } else {
                                "${((weather.current.temp_c * 9.0 / 5.0) + 32.0).toInt()}°F"
                            }
                            val condition = weather.current.condition.text

                            WeatherNotificationHelper.sendDailyBriefingNotification(
                                context = context,
                                cityName = cityName,
                                temp = temp,
                                condition = condition
                            )
                        } catch (_: Exception) {
                            WeatherNotificationHelper.sendDailyBriefingNotification(
                                context = context,
                                cityName = "Device Location",
                                temp = "--",
                                condition = "Good morning! Weather check active for your area."
                            )
                        }
                    } else {
                        WeatherNotificationHelper.sendDailyBriefingNotification(
                            context = context,
                            cityName = "Device Location",
                            temp = "--",
                            condition = "Good morning! Please enable device location for local weather updates."
                        )
                    }

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

