package ru.gabbasov.weatherapp.model

data class City(

    var id: Int,
    var name: String,
    var coord: Coord,
    var country: String
)
