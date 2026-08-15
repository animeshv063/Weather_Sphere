package com.example.weathersphere.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage

@Composable
fun WeatherIcon(
    iconUrl: String,
    modifier: Modifier = Modifier
) {

    AsyncImage(
        model = "https:$iconUrl",
        contentDescription = "Weather Icon",
        modifier = modifier
    )
}