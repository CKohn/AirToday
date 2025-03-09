package br.com.fiap.airtoday.repository

import android.util.Log
import br.com.fiap.airtoday.model.AirToday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AirTodayRepository {

    private const val API_KEY = "f6014cc8ee00cccbf35170333944b345"

    /**
     * Obtém os dados combinados de qualidade do ar (AQI), temperatura, umidade e nome da cidade.
     */
    suspend fun listaQualidadesAr(lat: Double, lon: Double): AirToday? {
        return withContext(Dispatchers.IO) {
            try {
                val cityName = obterNomeCidade(lat, lon)
                val aqi = obterIndiceDeQualidadeDoAr(lat, lon)
                val weatherData = obterDadosClimaticos(lat, lon)

                if (weatherData != null && aqi != null) {
                    return@withContext AirToday(
                        city = cityName,
                        aqi = aqi,
                        temperature = weatherData.first,
                        humidity = weatherData.second,
                        timestamp = System.currentTimeMillis()
                    )
                }
                return@withContext null
            } catch (e: Exception) {
                Log.e("API_ERROR", "Erro ao buscar dados: ${e.message}")
                return@withContext null
            }
        }
    }

    /**
     * Obtém os dados de temperatura e umidade usando Retrofit.
     */
    private suspend fun obterDadosClimaticos(lat: Double, lon: Double): Pair<Double?, Int?>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.getWeather(lat, lon, "metric", API_KEY)
                if (response.isSuccessful) {
                    val data = response.body()
                    return@withContext Pair(data?.main?.temperature, data?.main?.humidity)
                } else {
                    Log.e("API_ERROR", "Erro ao buscar dados climáticos: ${response.code()}")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Erro ao obter dados climáticos: ${e.message}")
                return@withContext null
            }
        }
    }

    /**
     * Obtém o Índice de Qualidade do Ar (AQI) usando Retrofit.
     */
    private suspend fun obterIndiceDeQualidadeDoAr(lat: Double, lon: Double): Int? {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.getAirQuality(lat, lon, API_KEY)
                if (response.isSuccessful) {
                    val airQualityResponse = response.body()
                    val list = airQualityResponse?.list
                    if (!list.isNullOrEmpty()) {
                        return@withContext list[0].main.aqi
                    }
                }
                return@withContext null
            } catch (e: Exception) {
                Log.e("API_ERROR", "Erro ao obter qualidade do ar: ${e.message}")
                return@withContext null
            }
        }
    }

    /**
     * Obtém o nome da cidade usando Retrofit.
     */
    suspend fun obterNomeCidade(lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.getCityName(lat, lon, 1, API_KEY)
                if (response.isSuccessful) {
                    val list = response.body()
                    if (!list.isNullOrEmpty()) {
                        return@withContext list[0].name
                    }
                }
                return@withContext "Localização Desconhecida"
            } catch (e: Exception) {
                Log.e("API_ERROR", "Erro ao obter nome da cidade: ${e.message}")
                return@withContext "Localização Desconhecida"
            }
        }
    }
}