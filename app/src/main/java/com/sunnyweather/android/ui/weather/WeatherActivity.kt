package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale


class WeatherActivity : AppCompatActivity() {
    lateinit var binding: ActivityWeatherBinding
    val viewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(viewModel.locationLng.isEmpty()){
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if(viewModel.locationLat.isEmpty()){
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if(viewModel.placeName.isEmpty()){
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
//        Log.d("WeatherActivity", "placeName:${viewModel.placeName}")
//        Log.d("WeatherActivity", "locationLng:${viewModel.locationLng}")
//        Log.d("WeatherActivity", "locationLat:${viewModel.locationLat}")
        viewModel.weatherLiveData.observe(this,Observer{ result ->
            val weather = result.getOrNull()
            if (weather != null) {
                Log.d("WeatherActivity", "weather:${weather}")
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        })
        //下拉刷新
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        refreshWeather()
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        //滑动菜单
        binding.navBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerSlide(p0: View, p1: Float) {}

            override fun onDrawerOpened(p0: View) {}

            override fun onDrawerClosed(p0: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                manager.hideSoftInputFromWindow(p0.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerStateChanged(p0: Int) {}
        })
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        binding.swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather) {
        findViewById<TextView>(R.id.placeName).text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        //填充now.xml布局中的数据
//        val bindingNow = NowBinding.inflate(layoutInflater)

        val currentTempText = "${realtime.temperature.toInt()} ℃"
        binding.currentTemp.text = currentTempText
        binding.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        binding.currentAQI.text = currentPM25Text
        binding.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        //填充forecast.xml布局中的数据
//        val bindingForecast = ForecastBinding.inflate(layoutInflater)
        binding.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for(i in 0 until days){
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                binding.forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            binding.forecastLayout.addView(view)
        }
//        setContentView(bindingForecast.root)
        //填充life_index.xml布局中的数据
//        val bindingLifeIndex = LifeIndexBinding.inflate(layoutInflater)
        val lifeIndex = daily.lifeIndex
        binding.coldRiskText.text = lifeIndex.coldRisk[0].desc
        binding.dressingText.text = lifeIndex.dressing[0].desc
        binding.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        binding.carWashingText.text = lifeIndex.carWashing[0].desc
        binding.weatherLayout.visibility = View.VISIBLE
//        setContentView(bindingLifeIndex.root)

    }
}

