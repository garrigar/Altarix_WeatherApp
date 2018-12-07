package ru.gabbasov.weatherapp.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import ru.gabbasov.weatherapp.common.Common
import ru.gabbasov.weatherapp.model.ForecastResult
import ru.gabbasov.weatherapp.R

/**
 * This class is an adapter to Recycler View for forecast screen
 */
class ForecastAdapter(
    private val context: Context?,
    private val forecastResult: ForecastResult
) : RecyclerView.Adapter<ForecastAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgWeather: ImageView = itemView.findViewById(R.id.image_card_weather) as ImageView
        val txtDateTime: TextView = itemView.findViewById(R.id.txt_card_date_time) as TextView
        val txtDescription: TextView = itemView.findViewById(R.id.txt_card_description) as TextView
        val txtTemperature: TextView = itemView.findViewById(R.id.txt_card_temp) as TextView
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_weather_forecast, parent, false)
    )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        with(holder) {
            (forecastResult.list[position]).let {
                // load icon
                Picasso.get().load("https://openweathermap.org/img/w/${it.weather[0].icon}.png")
                    .into(this.imgWeather)

                txtDateTime.text = Common.convertUnixToDate(it.dt)
                txtDescription.text = it.weather[0].description
                txtTemperature.text = StringBuilder(Common.formatReal(it.main.temp)).append("°C").toString()
            }
        }
    }

    override fun getItemCount() = forecastResult.list.size

}