package com.example.weathersphere.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class LocationHelper(
    private val context: Context
) {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (Double, Double) -> Unit,
        onError: () -> Unit = {}
    ) {
        if (!hasLocationPermission(context)) {
            onError()
            return
        }

        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    onSuccess(loc.latitude, loc.longitude)
                } else {
                    client.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            onSuccess(lastLoc.latitude, lastLoc.longitude)
                        } else {
                            onError()
                        }
                    }.addOnFailureListener { onError() }
                }
            }
            .addOnFailureListener {
                client.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        onSuccess(lastLoc.latitude, lastLoc.longitude)
                    } else {
                        onError()
                    }
                }.addOnFailureListener { onError() }
            }
    }

    companion object {
        fun hasLocationPermission(context: Context): Boolean {
            val coarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val fine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            return coarse || fine
        }

        /**
         * Reliably retrieves the device's physical GPS/Network coordinates.
         * Allocates sufficient timeout for GPS acquisition so notifications reflect the actual device location.
         */
        @SuppressLint("MissingPermission")
        suspend fun getDeviceLocation(context: Context): Location? {
            if (!hasLocationPermission(context)) return null

            return try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)

                // 1. Try High Accuracy Fused Location (up to 8 seconds for GPS fix)
                val highAccuracyLoc: Location? = withTimeoutOrNull(8000L) {
                    suspendCancellableCoroutine { continuation ->
                        val cts = CancellationTokenSource()
                        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                            .addOnSuccessListener { loc ->
                                if (continuation.isActive) continuation.resume(loc)
                            }
                            .addOnFailureListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                    }
                }
                if (highAccuracyLoc != null) return highAccuracyLoc

                // 2. Try Balanced Power Accuracy Fused Location (up to 5 seconds)
                val balancedLoc: Location? = withTimeoutOrNull(5000L) {
                    suspendCancellableCoroutine { continuation ->
                        val cts = CancellationTokenSource()
                        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                            .addOnSuccessListener { loc ->
                                if (continuation.isActive) continuation.resume(loc)
                            }
                            .addOnFailureListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                    }
                }
                if (balancedLoc != null) return balancedLoc

                // 3. Check cached lastLocation from Fused Provider
                val lastLoc: Location? = withTimeoutOrNull(2000L) {
                    suspendCancellableCoroutine { continuation ->
                        fusedClient.lastLocation
                            .addOnSuccessListener { loc ->
                                if (continuation.isActive) continuation.resume(loc)
                            }
                            .addOnFailureListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                    }
                }
                if (lastLoc != null) return lastLoc

                // 4. Fallback to Android System LocationManager
                val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locManager != null) {
                    val gpsLoc = try { locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null }
                    val netLoc = try { locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null }
                    val passLoc = try { locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (_: Exception) { null }

                    val bestLoc = listOfNotNull(gpsLoc, netLoc, passLoc).maxByOrNull { it.time }
                    if (bestLoc != null) return bestLoc

                    // If API 30+, try direct LocationManager.getCurrentLocation
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val lmFreshLoc: Location? = withTimeoutOrNull(4000L) {
                            suspendCancellableCoroutine { continuation ->
                                val provider = if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    LocationManager.GPS_PROVIDER
                                } else {
                                    LocationManager.NETWORK_PROVIDER
                                }
                                try {
                                    locManager.getCurrentLocation(
                                        provider,
                                        null,
                                        context.mainExecutor
                                    ) { loc ->
                                        if (continuation.isActive) continuation.resume(loc)
                                    }
                                } catch (_: Exception) {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                            }
                        }
                        if (lmFreshLoc != null) return lmFreshLoc
                    }
                }

                null
            } catch (_: Exception) {
                null
            }
        }

        // Backward compatibility
        suspend fun getApproximateLocation(context: Context): Location? {
            return getDeviceLocation(context)
        }
    }
}