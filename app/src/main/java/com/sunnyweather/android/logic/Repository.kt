package com.sunnyweather.android.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object Repository {

    fun searchPlaces(query: String) = fire(Dispatchers.IO){
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if(placeResponse.status == "ok"){
            val places = placeResponse.places
            Result.success(places)
        }else{
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }


    fun refreshWeather(lng: String, lat: String) = liveData(Dispatchers.IO){
        val result = try {
            coroutineScope {
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if(realtimeResponse.status == "ok" && dailyResponse.status == "ok"){
                val weather = Weather(realtimeResponse.result.realtime,
                    dailyResponse.result.daily)
                Result.success(weather)
            }else{
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }

        }
        }catch (e: Exception){
            Log.d("Repository", "${e.message}")
            Result.failure<Weather>(e)
        }
        emit(result)
    }
    private fun<T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
//                Log.d("Repository", "fire:${context}")
                block()
            } catch (e: Exception) {
//                Log.d("Repository", "fire: ${e.message}")
                Result.failure<T>(e)
            }
            emit(result)
        }
}