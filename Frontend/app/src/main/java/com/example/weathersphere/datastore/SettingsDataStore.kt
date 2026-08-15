package com.example.weathersphere.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "settings"
)

object SettingsDataStore {

    private val IS_CELSIUS = booleanPreferencesKey("is_celsius")
    private val IS_CANVAS_ANIMATION_ENABLED = booleanPreferencesKey("is_canvas_animation_enabled")
    private val IS_DAILY_BRIEFING_ENABLED = booleanPreferencesKey("is_daily_briefing_enabled")
    private val IS_SEVERE_ALERTS_ENABLED = booleanPreferencesKey("is_severe_alerts_enabled")
    private val DAILY_BRIEFING_TIME = stringPreferencesKey("daily_briefing_time")

    suspend fun saveTemperatureUnit(context: Context, isCelsius: Boolean) {
        context.dataStore.edit { it[IS_CELSIUS] = isCelsius }
    }

    fun getTemperatureUnit(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { it[IS_CELSIUS] ?: true }
    }

    suspend fun saveCanvasAnimationEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[IS_CANVAS_ANIMATION_ENABLED] = enabled }
    }

    fun getCanvasAnimationEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { it[IS_CANVAS_ANIMATION_ENABLED] ?: true }
    }

    suspend fun saveDailyBriefingEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[IS_DAILY_BRIEFING_ENABLED] = enabled }
    }

    fun getDailyBriefingEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { it[IS_DAILY_BRIEFING_ENABLED] ?: false }
    }

    suspend fun saveSevereAlertsEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[IS_SEVERE_ALERTS_ENABLED] = enabled }
    }

    fun getSevereAlertsEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { it[IS_SEVERE_ALERTS_ENABLED] ?: true }
    }

    suspend fun saveDailyBriefingTime(context: Context, time: String) {
        context.dataStore.edit { it[DAILY_BRIEFING_TIME] = time }
    }

    fun getDailyBriefingTime(context: Context): Flow<String> {
        return context.dataStore.data.map { it[DAILY_BRIEFING_TIME] ?: "07:00 AM" }
    }
}