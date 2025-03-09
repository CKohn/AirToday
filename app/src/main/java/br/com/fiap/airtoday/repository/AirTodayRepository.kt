package br.com.fiap.airtoday.repository

import android.util.Log
import br.com.fiap.airtoday.model.AirToday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AirTodayRepository {

    private const val API_KEY = "f6014cc8ee00cccbf35170333944b345"
    private const val BASE_URL_WEATHER = "https://api.openweathermap.org/data/2.5/weather"
    private const val BASE_URL_AIR_POLLUTION = "https://api.openweathermap.org/data/2.5/air_pollution"
    private const val BASE_URL_REVERSE_GEOCODING = "http://api.openweathermap.org/geo/1.0/reverse"

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
                        aqi = aqi, // 🔹 Apenas o índice de qualidade do ar
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
     * Obtém os dados de temperatura e umidade a partir da API OpenWeather `weather`.
     */
    private fun obterDadosClimaticos(lat: Double, lon: Double): Pair<Double?, Int?>? {
        return try {
            val urlString = "$BASE_URL_WEATHER?lat=$lat&lon=$lon&units=metric&appid=$API_KEY"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                val main = jsonObject.getJSONObject("main")
                val temperature = main.getDouble("temp")
                val humidity = main.getInt("humidity")

                Pair(temperature, humidity)
            } else {
                Log.e("API_ERROR", "Erro na requisição de clima: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Erro ao obter dados climáticos: ${e.message}")
            null
        }
    }

    /**
     * Obtém o Índice de Qualidade do Ar (AQI) a partir da API OpenWeather `air_pollution`.
     */
    private fun obterIndiceDeQualidadeDoAr(lat: Double, lon: Double): Int? {
        return try {
            val urlString = "$BASE_URL_AIR_POLLUTION?lat=$lat&lon=$lon&appid=$API_KEY"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                val list = jsonObject.getJSONArray("list")
                if (list.length() == 0) return null

                val data = list.getJSONObject(0)
                val main = data.getJSONObject("main")

                main.getInt("aqi") // 🔹 Retorna apenas o AQI
            } else {
                Log.e("API_ERROR", "Erro na requisição de qualidade do ar: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Erro ao obter qualidade do ar: ${e.message}")
            null
        }
    }

    /**
     * Obtém o nome da cidade a partir das coordenadas (latitude/longitude) usando a API OpenWeather Reverse Geocoding.
     */
    suspend fun obterNomeCidade(lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "https://api.openweathermap.org/geo/1.0/reverse?lat=$lat&lon=$lon&limit=1&appid=$API_KEY"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(response)

                    if (jsonArray.length() > 0) {
                        return@withContext jsonArray.getJSONObject(0).getString("name") // Obtém o nome da cidade corretamente
                    }
                }
                return@withContext "Localização Desconhecida" // Retorno caso não encontre nada
            } catch (e: Exception) {
                Log.e("API_ERROR", "Erro ao obter nome da cidade: ${e.message}")
                return@withContext "Localização Desconhecida"
            }
        }
    }

}
