package ru.gabbasov.weatherapp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.gabbasov.weatherapp.adapter.ForecastAdapter
import ru.gabbasov.weatherapp.common.Common
import ru.gabbasov.weatherapp.model.ForecastResult
import ru.gabbasov.weatherapp.retrofit.IOpenWeatherMap
import ru.gabbasov.weatherapp.retrofit.RetrofitClient

/**
 * Fragment for 2nd screen, with weather forecast
 */
class ForecastFragment : Fragment() {

    // factory method
    companion object {
        @JvmStatic
        fun newInstance() = ForecastFragment()
    }

    // RxJava & Retrofit
    private val compositeDisposable = CompositeDisposable()
    private val mService = RetrofitClient.instance.create(IOpenWeatherMap::class.java)

    // views with future data
    private lateinit var txtCityName: TextView
    private lateinit var txtGeoCoord: TextView

    private lateinit var recyclerForecast: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_forecast, container, false).apply {

            // init views
            txtCityName = this.findViewById(R.id.txt_forecast_city_name) as TextView
            txtGeoCoord = this.findViewById(R.id.txt_forecast_geo_coord) as TextView

            recyclerForecast = this.findViewById(R.id.recycler_forecast) as RecyclerView
            recyclerForecast.setHasFixedSize(true)
            recyclerForecast.layoutManager = LinearLayoutManager(
                this@ForecastFragment.context,
                LinearLayoutManager.VERTICAL,
                false
            )

            // executing request
            getForecastInfo()
        }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    private fun getForecastInfo() {
        // check internet
        Common.checkConnectionMessage(activity)

        // Get and parse response using Retrofit and RxJava
        compositeDisposable.add(
            mService.getForecastByLatLon(
                (Common.current_location?.latitude).toString(),
                (Common.current_location?.longitude).toString(),
                Common.APP_ID,
                "metric"
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    object : Consumer<ForecastResult> {
                        override fun accept(fr: ForecastResult?) {
                            fr?.let {

                                // it can be that there ain't cities in particular location
                                // in that case that whole "city" model won't be initialised and actually will contain null
                                // to handle that case, this kinda thing is applied
                                val actualName: String? = it.city.name

                                txtCityName.text = if (actualName.isNullOrBlank()) ""
                                else StringBuilder(it.city.name).append(", ", it.city.country).toString()

                                txtGeoCoord.text = if (actualName.isNullOrBlank()) ""
                                else it.city.coord.toString()

                                recyclerForecast.adapter = ForecastAdapter(context, it)

                            }
                        }
                    },
                    object : Consumer<Throwable> {
                        override fun accept(t: Throwable?) {
                            // Better to not show to user any scary text here
                            //Toast.makeText(activity, t?.message, Toast.LENGTH_LONG).show()
                            Log.e("Forecast Error", t?.message, t)
                        }
                    })

        )

    }

}
