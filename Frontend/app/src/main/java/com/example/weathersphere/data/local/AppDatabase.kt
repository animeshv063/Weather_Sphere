package com.example.weathersphere.data.local

import android.content.Context
import androidx.room.Room

object AppDatabase {

    @Volatile
    private var INSTANCE: WeatherDatabase? = null

    fun getDatabase(
        context: Context
    ): WeatherDatabase {

        return INSTANCE ?: synchronized(this) {

            val instance = Room.databaseBuilder(
                context.applicationContext,
                WeatherDatabase::class.java,
                "weather_database"
            ).build()

            INSTANCE = instance

            instance
        }
    }
}