package ru.gabbasov.weatherapp.retrofit

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import ru.gabbasov.weatherapp.model.ForecastResult
import ru.gabbasov.weatherapp.model.WeatherResult

/**
 * Interface for Retrofit, contains three basic queries (one for each screen)
 */
interface IOpenWeatherMap {

    /**
     * Get current weather info by latitude and longitude
     */
    @GET("weather")
    fun getWeatherByLatLon(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Observable<WeatherResult>

    /**
     * Get current weather info by city name
     */
    @GET("weather")
    fun getWeatherByCity(
        @Query("q") q: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Observable<WeatherResult>

    /**
     * Get weather forecast on 5 days by latitude and longitude
     */
    @GET("forecast")
    fun getForecastByLatLon(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Observable<ForecastResult>

}
