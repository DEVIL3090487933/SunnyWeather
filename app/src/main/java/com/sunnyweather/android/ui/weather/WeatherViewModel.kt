package com.sunnyweather.android.ui.weather

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location

class WeatherViewModel : ViewModel() {
    private val locationLiveData = MutableLiveData<Location>()

    var locationLng = ""
    var locationLat = ""
    var placeName = ""
    val weatherLiveData = locationLiveData.switchMap { location ->
        Repository.refreshWeather(location.lng, location.lat)
    }
    fun refreshWeather(lng: String, lat: String) {
        locationLiveData.value = Location(lng, lat)
//        Log.d("WeatherViewModel", "placeName:${placeName}")
//        Log.d("WeatherViewModel", "locationLng:${locationLng}")
//        Log.d("WeatherViewModel", "locationLat:${locationLat}")
    }
}