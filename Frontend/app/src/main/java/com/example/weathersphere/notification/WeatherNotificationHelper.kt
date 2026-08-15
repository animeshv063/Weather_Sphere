package com.example.weathersphere.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.weathersphere.R

object WeatherNotificationHelper {

    private const val CHANNEL_ID_BRIEFING = "daily_briefing_channel"
    private const val CHANNEL_NAME_BRIEFING = "Daily Morning Briefing"

    private const val CHANNEL_ID_ALERTS = "severe_weather_alerts"
    private const val CHANNEL_NAME_ALERTS = "Severe Weather Alerts"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Daily Briefing Channel
            val briefingChannel = NotificationChannel(
                CHANNEL_ID_BRIEFING,
                CHANNEL_NAME_BRIEFING,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sends daily weather summary notifications with physical sound"
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }

            // Severe Alerts Channel
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                CHANNEL_NAME_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Push warnings for severe weather events"
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }

            notificationManager.createNotificationChannel(briefingChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    fun sendDailyBriefingNotification(
        context: Context,
        cityName: String = "Tokyo",
        temp: String = "24°C",
        condition: String = "Clear Sky"
    ) {
        createNotificationChannels(context)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_BRIEFING)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("☀️ Morning Weather Briefing for $cityName")
            .setContentText("Current weather is $temp with $condition. High UV index around noon — have a wonderful day!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }

    fun sendSevereAlertNotification(
        context: Context,
        title: String = "⚠️ Severe Weather Warning",
        message: String = "Heavy rainfall & thunderstorm warning in effect for your area."
    ) {
        createNotificationChannels(context)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setAutoCancel(true)

        notificationManager.notify(1002, builder.build())
    }
}
