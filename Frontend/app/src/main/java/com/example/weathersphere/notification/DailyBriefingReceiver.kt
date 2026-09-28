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

                    // Strict requirement: "and if location not accessible then dont give notification"
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
                            val humidity = weather.current.humidity
                            val windKph = weather.current.wind_kph.toInt()

                            WeatherNotificationHelper.sendDailyBriefingNotification(
                                context = context,
                                cityName = cityName,
                                temp = temp,
                                condition = condition,
                                humidity = humidity,
                                windKph = windKph
                            )
                        } catch (_: Exception) {
                            // If weather cannot be retrieved for current location, do not show strange or fallback texts
                        }
                    }

                    // Reschedule for next 12h slot
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

