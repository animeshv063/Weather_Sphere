package com.example.weathersphere.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weathersphere.datastore.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isEnabled = SettingsDataStore.getDailyBriefingEnabled(context).first()
                    if (isEnabled) {
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
}
