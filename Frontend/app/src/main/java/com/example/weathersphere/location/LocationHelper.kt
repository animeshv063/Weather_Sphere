package com.example.weathersphere.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices

class LocationHelper(
    private val context: Context
) {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (Double, Double) -> Unit
    ) {

        val client =
            LocationServices
                .getFusedLocationProviderClient(context)

        client.lastLocation.addOnSuccessListener {

            if (it != null) {

                onSuccess(
                    it.latitude,
                    it.longitude
                )
            }
        }
    }
}