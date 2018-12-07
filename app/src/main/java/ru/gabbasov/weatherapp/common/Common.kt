package ru.gabbasov.weatherapp.common

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

/**
 * This object contains some common things, that are used in different places
 */
object Common {

    /** OpenWeatherMap key */
    const val APP_ID = "7960bfb16e32b114d22cb691a50b0ba0"

    /** current known location will me stored here and thus will be accessible from different places */
    var current_location: Location? = null

    /** converts amount of seconds in UNIX date format to a specified custom format */
    fun convertUnixToDate(dt: Int): String =
        SimpleDateFormat("HH:mm EEE dd.MM.yyyy", Locale.US).format(Date(dt * 1000L))

    fun convertUnixToHour(dt: Int): String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(dt * 1000L))

    /** rounds the number to the <@param afterDot> digits after a decimal dot */
    fun formatReal(d: Double, afterDot: Int = 1) = String.format(Locale.US, "%.${afterDot}f", d)

    /** allows us to get a wind direction as if it was on compass */
    fun getWindCompass(x: Double) = when {
        ((337.5 < x) || (x <= 22.5)) -> "N"
        ((22.5 < x) && (x <= 67.5)) -> "NE"
        ((67.5 < x) && (x <= 112.5)) -> "E"
        ((112.5 < x) && (x <= 157.5)) -> "SE"
        ((157.5 < x) && (x <= 202.5)) -> "S"
        ((202.5 < x) && (x <= 247.5)) -> "SW"
        ((247.5 < x) && (x <= 292.5)) -> "W"
        else -> "NW"
    }

    /** hPa to millimetres mercury */
    fun hpaToMmHg(hpa: Double): Double = (0.75006 * hpa)

    /** If there is not an internet connection, it makes a toast, reminding this */
    fun checkConnectionMessage(context: Context?) {
        val netInfo = (context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (netInfo == null || !netInfo.isConnected)
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
    }

}
