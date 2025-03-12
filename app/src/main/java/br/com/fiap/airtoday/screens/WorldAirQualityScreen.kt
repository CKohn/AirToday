package br.com.fiap.airtoday.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.fiap.airtoday.R
import br.com.fiap.airtoday.repository.AirTodayRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorldCity(
    val cityName: String,
    val latitude: Double,
    val longitude: Double
)

data class CityAirQuality(
    val cityName: String,
    val aqi: Int?,
    val temperature: Double?,
    val humidity: Int?,
    val lastUpdate: String
)

private val capitals = listOf(
    WorldCity("New York", 40.7128, -74.0060),
    WorldCity("London", 51.5074, -0.1278),
    WorldCity("Tokyo", 35.6895, 139.6917),
    WorldCity("Brasília", -15.7939, -47.8828),
    WorldCity("Paris", 48.8566, 2.3522),
    WorldCity("Sydney", -33.8688, 151.2093),
    WorldCity("Moscow", 55.7558, 37.6173),
    WorldCity("Cairo", 30.0444, 31.2357),
    WorldCity("Beijing", 39.9042, 116.4074),
    WorldCity("Delhi", 28.7041, 77.1025)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldAirQualityScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var worldData by remember { mutableStateOf<List<CityAirQuality>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    fun fetchAllCapitals() {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            showError = false
            val resultList = mutableListOf<CityAirQuality>()
            try {
                for (city in capitals) {
                    val airToday = AirTodayRepository.listaQualidadesAr(city.latitude, city.longitude)
                    if (airToday != null) {
                        val item = CityAirQuality(
                            cityName = city.cityName,
                            aqi = airToday.aqi,
                            temperature = airToday.temperature,
                            humidity = airToday.humidity,
                            lastUpdate = dateFormat.format(Date())
                        )
                        resultList.add(item)
                    }
                }
                withContext(Dispatchers.Main) {
                    worldData = resultList
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError = true
                    isLoading = false
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        fetchAllCapitals()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Air Quality in the World") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (showError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Error loading data.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(worldData) { cityData ->
                        CityAirCard(cityData)
                    }
                }
            }
        }
    }
}

@Composable
fun CityAirCard(cityData: CityAirQuality) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cityData.cityName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            cityData.aqi?.let { aqi ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = when (aqi) {
                                    1 -> listOf(Color(0xFF00E400), Color(0xFF008000))
                                    2 -> listOf(Color(0xFFFFFF00), Color(0xFFCCCC00))
                                    3 -> listOf(Color(0xFFFFA500), Color(0xFFD2691E))
                                    4 -> listOf(Color(0xFFFF0000), Color(0xFF8B0000))
                                    5 -> listOf(Color(0xFF800080), Color(0xFF4B0082))
                                    else -> listOf(Color.Gray, Color.DarkGray)
                                }
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AQI: $aqi",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Temperature: %.1f °C".format(cityData.temperature ?: 0.0), fontSize = 16.sp)
            Text("Humidity: %d %%".format(cityData.humidity ?: 0), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Last update: ${cityData.lastUpdate}", fontSize = 12.sp)
        }
    }
}
