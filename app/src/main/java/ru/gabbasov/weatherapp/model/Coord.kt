package ru.gabbasov.weatherapp.model

data class Coord(

    var lon: Double,
    var lat: Double

) {
    override fun toString(): String {
        return "[$lat, $lon]"
    }
}
