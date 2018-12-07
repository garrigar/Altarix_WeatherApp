package ru.gabbasov.weatherapp.model

data class MyList(

    var dt: Int,
    var main: Main,
    var weather: List<Weather>,
    var clouds: Clouds,
    var wind: Wind,
    var rain: Rain,
    var sys: Sys,
    var dt_txt: String
)
