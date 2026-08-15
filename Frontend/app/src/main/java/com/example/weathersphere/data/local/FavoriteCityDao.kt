package com.example.weathersphere.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteCityDao {

    @Insert
    suspend fun insertCity(city: FavoriteCity)

    @Delete
    suspend fun deleteCity(city: FavoriteCity)

    @Query("SELECT * FROM favorite_cities")
    suspend fun getAllCities(): List<FavoriteCity>
}

