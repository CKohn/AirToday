package br.com.fiap.airtoday.repository

import android.util.Log
import br.com.fiap.airtoday.model.AirToday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AirTodayRepository {

    private const val API_KEY = "f6014cc8ee00cccbf35170333944b345" // 🔹 Substitua pelo seu token real
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/air_pollution"

    suspend fun listaQualidadesAr(lat: Double, lon: Double): AirToday? {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "$BASE_URL?lat=$lat&lon=$lon&appid=$API_KEY"
                Log.d("API_REQUEST", "URL: $urlString") // 🔹 Debug para ver a URL gerada
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("API_RESPONSE", response) // 🔹 Log da resposta da API
                    return@withContext parseJson(response)
                } else {
                    Log.e("API_ERROR", "Erro na requisição: $responseCode")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Erro: ${e.message}")
                return@withContext null
            }
        }
    }

    private fun parseJson(jsonString: String): AirToday? {
        Log.d("PARSE_JSON", "JSON recebido: $jsonString") // 🔹 Log para verificar o JSON

        val jsonObject = JSONObject(jsonString)
        val list = jsonObject.getJSONArray("list")
        if (list.length() == 0) {
            Log.e("PARSE_ERROR", "Erro: Nenhum dado encontrado na lista")
            return null
        }

        val data = list.getJSONObject(0)
        val main = data.getJSONObject("main")
        val components = data.getJSONObject("components")

        return AirToday(
            city = "Localização Desconhecida", // OpenWeather não retorna o nome da cidade diretamente
            aqi = main.getInt("aqi"),
            temperature = null, // OpenWeather não fornece temperatura nessa API
            humidity = null, // OpenWeather não fornece umidade nessa API
            pm25 = components.optDouble("pm2_5"),
            pm10 = components.optDouble("pm10"),
            o3 = components.optDouble("o3"),
            no2 = components.optDouble("no2"),
            so2 = components.optDouble("so2"),
            co = components.optDouble("co"),
            timestamp = System.currentTimeMillis()
        )
    }
}
