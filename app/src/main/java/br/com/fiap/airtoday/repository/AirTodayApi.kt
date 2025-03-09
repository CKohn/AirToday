package br.com.fiap.airtoday.repository

import br.com.fiap.airtoday.model.CityResponse
import br.com.fiap.airtoday.model.WeatherResponse
import br.com.fiap.airtoday.model.AirQualityResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AirTodayApi {

    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): Response<WeatherResponse>

    @GET("data/2.5/air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Response<AirQualityResponse>

    @GET("geo/1.0/reverse")
    suspend fun getCityName(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): Response<List<CityResponse>>
}
