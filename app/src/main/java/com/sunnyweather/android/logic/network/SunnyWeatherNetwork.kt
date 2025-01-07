package com.sunnyweather.android.logic.network

import android.util.Log
import com.sunnyweather.android.logic.model.DailyResponse
import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork {
    private val placeService = ServiceCreator.create<PlaceService>()
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    private val weatherService = ServiceCreator.create<WeatherService>()

    suspend fun getDailyWeather(lng: String, lat: String) : DailyResponse {
//        Log.d("SunnyWeatherNetwork", "lng: $lng, lat: $lat")
        return weatherService.getDailyWeather(lng, lat).await()
    }
    suspend fun getRealtimeWeather(lng: String, lat: String) : RealtimeResponse {
//        Log.d("SunnyWeatherNetwork", "lng: $lng, lat: $lat")
        return weatherService.getRealtimeWeather(lng, lat).await()
    }

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    Log.d("SunnyWeatherNetwork", "response: $body")
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null"))
                }
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}