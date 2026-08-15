package com.example.weathersphere.ui.home

fun getWeatherTip(
    condition: String
): String {

    return when {

        condition.contains("rain", true) ->
            "Carry an umbrella ☔"

        condition.contains("sun", true) ->
            "Stay hydrated 💧"

        condition.contains("cloud", true) ->
            "Great weather for a walk 🚶"

        condition.contains("snow", true) ->
            "Wear warm clothes 🧥"

        else ->
            "Have a great day 🌤"
    }
}