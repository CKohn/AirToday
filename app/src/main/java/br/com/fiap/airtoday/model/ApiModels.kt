package br.com.fiap.airtoday.model

import com.google.gson.annotations.SerializedName

// Resposta para a API de Clima
data class WeatherResponse(
    @SerializedName("main") val main: MainData
)

data class MainData(
    @SerializedName("temp") val temperature: Double,
    @SerializedName("humidity") val humidity: Int
)

// Resposta para a API de Nome da Cidade (Geocoding)
data class CityResponse(
    @SerializedName("name") val name: String
)

// Resposta para a API de Qualidade do Ar
data class AirQualityResponse(
    @SerializedName("list") val list: List<AirQualityData>
)

data class AirQualityData(
    @SerializedName("main") val main: AirQualityMain
)

data class AirQualityMain(
    @SerializedName("aqi") val aqi: Int // Índice de Qualidade do Ar (AQI)
)
