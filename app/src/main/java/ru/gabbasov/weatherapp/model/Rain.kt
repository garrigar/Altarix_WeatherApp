package ru.gabbasov.weatherapp.model

import com.google.gson.annotations.SerializedName

data class Rain(

    @SerializedName("3h")
    var vol3h: Double
)
