package ru.gabbasov.weatherapp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.label305.asynctask.SimpleAsyncTask
import com.mancj.materialsearchbar.MaterialSearchBar
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.gabbasov.weatherapp.common.Common
import ru.gabbasov.weatherapp.model.WeatherResult
import ru.gabbasov.weatherapp.retrofit.IOpenWeatherMap
import ru.gabbasov.weatherapp.retrofit.RetrofitClient
import java.util.zip.GZIPInputStream

/**
 * Fragment for 3rd screen, with weather by city
 */
class CityFragment : Fragment() {

    // factory method
    companion object {
        @JvmStatic
        fun newInstance() = CityFragment()
    }

    // city list
    private lateinit var listCities: List<String>
    // search bar
    private lateinit var searchBar: MaterialSearchBar

    // different views with future data
    private lateinit var txtCityName: TextView

    private lateinit var imgWeather: ImageView

    private lateinit var txtTemp: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtDateTime: TextView

    private lateinit var txtWind: TextView
    private lateinit var txtPressure: TextView
    private lateinit var txtHumidity: TextView
    private lateinit var txtSunrise: TextView
    private lateinit var txtSunset: TextView
    private lateinit var txtGeoCoord: TextView

    private lateinit var weatherPanel: LinearLayout
    private lateinit var progressBar: ProgressBar

    // RxJava & Retrofit
    private val compositeDisposable = CompositeDisposable()
    private val mService = RetrofitClient.instance.create(IOpenWeatherMap::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_city, container, false).apply {

        // init views
        txtCityName = this.findViewById(R.id.city_txt_city_name) as TextView

        imgWeather = this.findViewById(R.id.city_image_weather) as ImageView

        txtTemp = this.findViewById(R.id.city_txt_temp) as TextView
        txtDescription = this.findViewById(R.id.city_txt_description) as TextView
        txtDateTime = this.findViewById(R.id.city_txt_date_time) as TextView

        txtWind = this.findViewById(R.id.city_txt_wind) as TextView
        txtPressure = this.findViewById(R.id.city_txt_pressure) as TextView
        txtHumidity = this.findViewById(R.id.city_txt_humidity) as TextView
        txtSunrise = this.findViewById(R.id.city_txt_sunrise) as TextView
        txtSunset = this.findViewById(R.id.city_txt_sunset) as TextView
        txtGeoCoord = this.findViewById(R.id.city_txt_geo_coord) as TextView

        weatherPanel = this.findViewById(R.id.city_weather_panel) as LinearLayout
        progressBar = this.findViewById(R.id.city_progressBar) as ProgressBar

        searchBar = this.findViewById(R.id.search_bar) as MaterialSearchBar
        searchBar.isEnabled = false

        // execute an Async Task to load and setup our city list
        LoadCities().execute<LoadCities>()

    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    private inner class LoadCities : SimpleAsyncTask<List<String>>() {

        override fun doInBackgroundSimple(): List<String> {
            listCities = ArrayList()

            // making a buffered input stream from raw resource: an GZip archive with JSON-formatted cities in it
            GZIPInputStream(resources.openRawResource(R.raw.city_list_min)).bufferedReader()
                .use {
                    val builder = StringBuilder()
                    var readed = it.readLine()
                    while (readed != null) {
                        builder.append(readed)
                        readed = it.readLine()
                    }
                    // parsing json
                    listCities = Gson().fromJson(builder.toString(), object : TypeToken<List<String>>() {}.type)
                }
            return listCities
        }

        override fun onSuccess(listCity: List<String>) {
            super.onSuccess(listCity)

            // setting up the search bar
            searchBar.isEnabled = true
            searchBar.addTextChangeListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // we are giving to users suggestions basing on text that was already inputted
                    // basically suggesting words containing search bar input as a substring
                    searchBar.lastSuggestions = ArrayList<String>().apply {
                        for (city in listCity) {
                            if (city.contains(searchBar.text, true))
                                add(city)
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            searchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
                override fun onButtonClicked(buttonCode: Int) {}

                override fun onSearchStateChanged(enabled: Boolean) {}

                override fun onSearchConfirmed(text: CharSequence?) {
                    // executing request
                    getWeatherInfo(text.toString())

                    searchBar.lastSuggestions = listCity
                }
            })

            searchBar.lastSuggestions = listCity

            // hide loading roller
            progressBar.visibility = View.GONE

        }

    }

    private fun getWeatherInfo(city: String) {
        // check internet
        Common.checkConnectionMessage(activity)

        // Get and parse response using Retrofit and RxJava
        compositeDisposable.add(
            mService.getWeatherByCity(
                city,
                Common.APP_ID,
                "metric"
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    object : Consumer<WeatherResult> {
                        override fun accept(wr: WeatherResult?) {

                            wr?.let {
                                // load image
                                Picasso.get().load("https://openweathermap.org/img/w/${it.weather[0].icon}.png")
                                    .into(imgWeather)

                                // load info
                                txtCityName.text = StringBuilder(it.name).append(", ", it.sys.country).toString()

                                txtTemp.text = StringBuilder(Common.formatReal(it.main.temp)).append("°C").toString()
                                txtDescription.text = it.weather[0].description
                                txtDateTime.text = Common.convertUnixToDate(it.dt)

                                txtWind.text = StringBuilder(Common.formatReal(it.wind.speed)).append(" m/s, ")
                                    .append(Common.formatReal(it.wind.deg)).append("°")
                                    .append(" (").append(Common.getWindCompass(it.wind.deg)).append(")").toString()
                                txtPressure.text =
                                        StringBuilder(Common.formatReal(it.main.pressure)).append(" hPa | ")
                                            .append(Common.formatReal(Common.hpaToMmHg(it.main.pressure)))
                                            .append(" mmHg")
                                            .toString()
                                txtHumidity.text = StringBuilder(it.main.humidity.toString()).append("%").toString()
                                txtSunrise.text = Common.convertUnixToHour(it.sys.sunrise)
                                txtSunset.text = Common.convertUnixToHour(it.sys.sunset)
                                txtGeoCoord.text = it.coord.toString()

                                // display weather panel
                                weatherPanel.visibility = View.VISIBLE
                                progressBar.visibility = View.GONE


                            }
                        }
                    },
                    object : Consumer<Throwable> {
                        // consuming errors here
                        override fun accept(t: Throwable?) {
                            // we must not be afraid to show toast here, because for example:
                            // if a user inputs a city not from list, he will be able to see 404 Not Found response,
                            // so it's useful
                            Toast.makeText(activity, t?.message, Toast.LENGTH_LONG).show()
                            Log.e("CityWeather Error", t?.message, t)
                        }
                    })

        )
    }


}
