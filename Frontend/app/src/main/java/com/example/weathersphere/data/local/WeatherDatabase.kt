package com.example.weathersphere.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(

    entities = [

        FavoriteCity::class

    ],

    version = 1,

    exportSchema = false

)

abstract class WeatherDatabase :
    RoomDatabase() {

    abstract fun favoriteCityDao():
            FavoriteCityDao
}