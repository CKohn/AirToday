package br.com.fiap.airtoday.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("main") val main: MainData
)

data class MainData(
    @SerializedName("temp") val temperature: Double,
    @SerializedName("humidity") val humidity: Int
)

data class CityResponse(
    @SerializedName("name") val name: String
)

data class AirQualityResponse(
    @SerializedName("list") val list: List<AirQualityData>
)

data class AirQualityData(
    @SerializedName("main") val main: AirQualityMain
)

data class AirQualityMain(
    @SerializedName("aqi") val aqi: Int
)
