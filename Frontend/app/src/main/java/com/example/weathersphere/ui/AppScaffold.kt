package com.example.weathersphere.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weathersphere.navigation.NavGraph
import com.example.weathersphere.ui.components.BottomBar
import com.example.weathersphere.ui.components.PageBackgroundCanvas
import com.example.weathersphere.viewmodel.WeatherViewModel

@Composable
fun AppScaffold() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val viewModel: WeatherViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val currentCondition = uiState.weather?.current?.condition?.text

    Box(modifier = Modifier.fillMaxSize()) {
        // Unique Page Background Canvas Animation covering true edge-to-edge screen
        PageBackgroundCanvas(
            pageIndex = selectedIndex,
            weatherCondition = currentCondition,
            isAnimationEnabled = uiState.isCanvasAnimationEnabled
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomBar(
                    selectedIndex = selectedIndex,
                    onItemSelected = { index -> selectedIndex = index }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Screen Content Navigation Graph
                NavGraph(
                    selectedIndex = selectedIndex,
                    onTabSelected = { index -> selectedIndex = index },
                    viewModel = viewModel
                )
            }
        }
    }
}