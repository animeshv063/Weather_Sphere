package com.example.weathersphere.ui.theme

import androidx.compose.ui.graphics.Color

fun getBackgroundColor(
    weather: String
): Color {

    return when {

        weather.contains("rain", true) ->
            Color(0xFF90CAF9)

        weather.contains("cloud", true) ->
            Color(0xFFCFD8DC)

        weather.contains("sun", true) ->
            Color(0xFFFFF59D)

        weather.contains("clear", true) ->
            Color(0xFF81D4FA)

        else ->
            Color.White
    }
}