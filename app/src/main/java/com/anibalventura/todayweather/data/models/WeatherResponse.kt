package com.anibalventura.todayweather.data.models

import java.io.Serializable

data class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val base: String,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Int,
    val sys: Sys,
    val id: Int,
    val name: String,
    val cod: Int
) : Serializable


data class Coord(
    val lon: Double,
    val lat: Double
) : Serializable

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) : Serializable

data class Main(
    val temp: Double,
    val pressure: Double,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double,
    val sea_level: Double,
    val grnd_level: Double
) : Serializable

data class Wind(
    val speed: Double,
    val deg: Int
) : Serializable

data class Clouds(
    val all: Int
) : Serializable

data class Sys(
    val type: Int,
    val message: Double,
    val country: String,
    val sunrise: Int,
    val sunset: Int
) : Serializable