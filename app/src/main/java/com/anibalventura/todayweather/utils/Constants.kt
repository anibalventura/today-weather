package com.anibalventura.todayweather.utils

import com.anibalventura.todayweather.utils.ApiKey.OPEN_WEATHER_API_KEY

object Constants {

    // Open Weather
    const val BASE_URL: String = "http://api.openweathermap.org/data/"
    const val METRIC_UNIT: String = "metric"
    const val API_KEY: String = OPEN_WEATHER_API_KEY

    // Progress Dialog
    const val SHOW_PROGRESS = "show_progress"
    const val HIDE_PROGRESS = "hide_progress"

    // Preferences
    const val PREFERENCE_NAME = "today_weather_preference"
    const val WEATHER_RESPONSE_DATA = "weather_response_data"
}