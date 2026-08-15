package com.example.weathersphere

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.weathersphere.location.LocationHelper
import com.example.weathersphere.notification.WeatherNotificationHelper
import com.example.weathersphere.ui.AppScaffold
import com.example.weathersphere.ui.theme.WeatherSphereTheme
import com.example.weathersphere.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            // Permissions granted/denied callback without auto-searching
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            WeatherNotificationHelper.createNotificationChannels(this)

            val permissionsToRequest = mutableListOf<String>()

            if (!LocationHelper.hasLocationPermission(this)) {
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            if (permissionsToRequest.isNotEmpty()) {
                permissionsLauncher.launch(permissionsToRequest.toTypedArray())
            }
        } catch (_: Exception) {
        }

        enableEdgeToEdge()

        setContent {
            WeatherSphereTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}