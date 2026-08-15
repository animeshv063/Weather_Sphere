package com.example.weathersphere.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weathersphere.ui.about.AboutScreen
import com.example.weathersphere.ui.favorites.FavoritesScreen
import com.example.weathersphere.ui.home.HomeScreen
import com.example.weathersphere.ui.settings.SettingsScreen
import com.example.weathersphere.viewmodel.WeatherViewModel

@Composable
fun NavGraph(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit = {},
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (selectedIndex) {
        0 -> {
            HomeScreen(
                uiState = uiState,
                onSearch = { viewModel.searchCity(it) },
                onTyping = { viewModel.searchSuggestions(it) },
                onAddFavorite = { viewModel.saveFavorite(it) }
            )
        }

        1 -> {
            FavoritesScreen(
                favorites = uiState.favorites,
                onDelete = { viewModel.deleteFavorite(it) },
                onSelectCity = { cityName ->
                    if (cityName.isNotBlank()) {
                        viewModel.searchCity(cityName)
                    }
                    onTabSelected(0)
                }
            )
        }

        2 -> {
            SettingsScreen(
                isCelsius = uiState.isCelsius,
                isCanvasAnimationEnabled = uiState.isCanvasAnimationEnabled,
                isDailyBriefingEnabled = uiState.isDailyBriefingEnabled,
                isSevereAlertsEnabled = uiState.isSevereAlertsEnabled,
                dailyBriefingTime = uiState.dailyBriefingTime,
                onUnitChanged = { isCelsius ->
                    viewModel.changeTemperatureUnit(isCelsius)
                },
                onToggleCanvasAnimation = { enabled ->
                    viewModel.toggleCanvasAnimation(enabled)
                },
                onToggleDailyBriefing = { enabled ->
                    viewModel.toggleDailyBriefing(enabled)
                },
                onToggleSevereAlerts = { enabled ->
                    viewModel.toggleSevereAlerts(enabled)
                },
                onReminderTimeChanged = { time ->
                    viewModel.changeDailyBriefingTime(time)
                }
            )
        }

        3 -> {
            AboutScreen()
        }
    }
}