package ru.gabbasov.weatherapp.adapter

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import ru.gabbasov.weatherapp.CityFragment
import ru.gabbasov.weatherapp.ForecastFragment
import ru.gabbasov.weatherapp.TodayWeatherFragment

/**
 * This class is an adapter to View Pager consists of Fragments
 */
class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val pageCount = 3

    override fun getCount() = pageCount

    override fun getItem(i: Int) = when (i) {
        0 -> TodayWeatherFragment.newInstance()
        1 -> ForecastFragment.newInstance()
        else -> CityFragment.newInstance()
    }

    override fun getPageTitle(i: Int) = when (i) {
        0 -> "Now"
        1 -> "Forecast"
        else -> "Cities"
    }

}
