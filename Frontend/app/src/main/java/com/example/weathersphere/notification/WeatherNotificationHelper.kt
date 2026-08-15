package com.example.weathersphere.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.weathersphere.MainActivity
import com.example.weathersphere.R
import java.util.Calendar

object WeatherNotificationHelper {

    private const val CHANNEL_ID_BRIEFING = "daily_briefing_channel"
    private const val CHANNEL_NAME_BRIEFING = "Daily Morning Briefing"

    private const val CHANNEL_ID_ALERTS = "severe_weather_alerts"
    private const val CHANNEL_NAME_ALERTS = "Severe Weather Alerts"

    private const val ACTION_DAILY_BRIEFING = "com.example.weathersphere.ACTION_DAILY_BRIEFING"
    private const val REQUEST_CODE_BRIEFING_ALARM = 2001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                        ?: return

                // Daily Briefing Channel (Pop-up banner, high priority & sound)
                val briefingChannel = NotificationChannel(
                    CHANNEL_ID_BRIEFING,
                    CHANNEL_NAME_BRIEFING,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Sends daily morning weather briefings with pop-up banner and sound"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 350, 200, 350)
                    if (soundUri != null) {
                        setSound(soundUri, audioAttributes)
                    }
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }

                // Severe Alerts Channel (Pop-up banner, high priority & sound)
                val alertsChannel = NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    CHANNEL_NAME_ALERTS,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Push warnings for severe weather events with pop-up banner and sound"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 250, 500)
                    if (soundUri != null) {
                        setSound(soundUri, audioAttributes)
                    }
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }

                notificationManager.createNotificationChannel(briefingChannel)
                notificationManager.createNotificationChannel(alertsChannel)
            } catch (_: Exception) {
            }
        }
    }

    fun scheduleDailyBriefing(context: Context, timeString: String = "07:00 AM") {
        try {
            createNotificationChannels(context)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, DailyBriefingReceiver::class.java).apply {
                action = ACTION_DAILY_BRIEFING
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BRIEFING_ALARM,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Parse target hour and minute
            val isPm = timeString.contains("PM", ignoreCase = true)
            val cleanTime = timeString.replace("AM", "").replace("PM", "").trim()
            val parts = cleanTime.split(":")
            var targetHour = parts.getOrNull(0)?.toIntOrNull() ?: 7
            if (isPm && targetHour < 12) targetHour += 12
            if (!isPm && targetHour == 12) targetHour = 0
            val targetMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val now = Calendar.getInstance()
            val targetTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, targetHour)
                set(Calendar.MINUTE, targetMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (targetTime.before(now) || targetTime.timeInMillis <= now.timeInMillis) {
                targetTime.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTime.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetTime.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
        }
    }

    fun cancelDailyBriefing(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, DailyBriefingReceiver::class.java).apply {
                action = ACTION_DAILY_BRIEFING
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BRIEFING_ALARM,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {
        }
    }

    fun sendDailyBriefingNotification(
        context: Context,
        cityName: String = "Tokyo",
        temp: String = "24°C",
        condition: String = "Clear Sky"
    ) {
        try {
            createNotificationChannels(context)

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_BRIEFING)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("☀️ Morning Weather Briefing • $cityName")
                .setContentText("Today: $temp with $condition. Have a wonderful day!")
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        "Good morning! Current conditions in $cityName: $temp, $condition.\nHave a wonderful day!"
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .apply {
                    if (soundUri != null) {
                        setSound(soundUri)
                    }
                }
                .setVibrate(longArrayOf(0, 350, 200, 350))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            notificationManager.notify(1001, builder.build())
        } catch (_: Exception) {
        }
    }

    fun sendSevereAlertNotification(
        context: Context,
        title: String = "⚠️ Severe Weather Warning",
        message: String = "Heavy rainfall & thunderstorm warning in effect for your area."
    ) {
        try {
            createNotificationChannels(context)

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .apply {
                    if (soundUri != null) {
                        setSound(soundUri)
                    }
                }
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            notificationManager.notify(1002, builder.build())
        } catch (_: Exception) {
        }
    }
}
