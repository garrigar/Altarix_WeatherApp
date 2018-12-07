package ru.gabbasov.weatherapp.model

data class ForecastResult(

    var cod: String,
    var message: Double,
    var cnt: Int,
    var list: List<MyList>,
    var city: City
)
