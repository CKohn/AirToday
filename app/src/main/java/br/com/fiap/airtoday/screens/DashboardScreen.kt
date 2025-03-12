package br.com.fiap.airtoday.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import br.com.fiap.airtoday.R
import br.com.fiap.airtoday.repository.AirTodayRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, initialLatitude: Double, initialLongitude: Double) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

    var latitude by remember { mutableStateOf(initialLatitude) }
    var longitude by remember { mutableStateOf(initialLongitude) }
    var airQualityIndex by remember { mutableStateOf<Int?>(null) }
    var cityName by remember { mutableStateOf<String?>(null) }
    var temperature by remember { mutableStateOf<Double?>(null) }
    var humidity by remember { mutableStateOf<Int?>(null) }
    var lastUpdate by remember { mutableStateOf("—") }

    // Estados de carregamento e erro
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // Formata data/hora
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    fun fetchAirQualityData() {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            hasError = false
            try {
                val airToday = AirTodayRepository.listaQualidadesAr(latitude, longitude)
                withContext(Dispatchers.Main) {
                    cityName = airToday?.city ?: ""
                    airQualityIndex = airToday?.aqi
                    temperature = airToday?.temperature
                    humidity = airToday?.humidity
                    lastUpdate = dateFormat.format(Date())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hasError = true
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun updateLocationAndFetchData() {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            hasError = false
            try {
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    withContext(Dispatchers.Main) {
                        hasError = true
                    }
                    return@launch
                }

                val lastLocation = fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                lastLocation?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                }

                withContext(Dispatchers.Main) {
                    fetchAirQualityData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hasError = true
                }
            }
        }
    }

    // Ao entrar na tela, atualiza localização e carrega dados
    LaunchedEffect(Unit) {
        updateLocationAndFetchData()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.dashboard_title)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Se está carregando, mostra loading
            if (isLoading) {
                CircularProgressIndicator()
            }
            // Se deu erro, mostra mensagem de erro
            else if (hasError) {
                Text(
                    text = stringResource(id = R.string.error_loading_data),
                    fontSize = 18.sp,
                    color = Color.Red
                )
            }
            // Caso contrário, mostra o Card com dados
            else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.city_label) + " " +
                                    (cityName?.ifEmpty { stringResource(id = R.string.unknown_location) } ?: ""),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(id = R.string.latitude_longitude_label, latitude, longitude),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Se não deu erro, mas ainda não temos airQualityIndex (pode ser nulo)
                        airQualityIndex?.let { aqi ->
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
                                    text = "${stringResource(id = R.string.aqi_label)} $aqi",
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(stringResource(id = R.string.last_update, lastUpdate), fontSize = 12.sp)
                            Text(stringResource(id = R.string.temperature_label, temperature ?: 0.0), fontSize = 16.sp)
                            Text(stringResource(id = R.string.humidity_label, humidity ?: 0), fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // LEGENDA
            AqiLegend()

            Spacer(modifier = Modifier.height(20.dp))

            // Botão para atualizar a localização e recarregar os dados
            Button(
                onClick = { updateLocationAndFetchData() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF025930))
            ) {
                Text(stringResource(id = R.string.refresh_data))
            }

            // Botão para ir ao histórico, passando o nome da cidade
            Button(
                onClick = {
                    navController.navigate("historico/${cityName ?: ""}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF025930))
            ) {
                Text("Ver Histórico")
            }
        }
    }
}

/**
 * Legenda dos valores de AQI (1 a 5).
 */
@Composable
fun AqiLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AQI (Air Quality Index)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            AqiLegendItem(aqiValue = 1, label = "Good", color = Color(0xFF00E400))
            AqiLegendItem(aqiValue = 2, label = "Fair", color = Color(0xFFFFFF00))
            AqiLegendItem(aqiValue = 3, label = "Moderate", color = Color(0xFFFFA500))
            AqiLegendItem(aqiValue = 4, label = "Poor", color = Color(0xFFFF0000))
            AqiLegendItem(aqiValue = 5, label = "Very Poor", color = Color(0xFF800080))
        }
    }
}

/**
 * Item individual da legenda (ex.: "AQI 1: Good")
 */
@Composable
fun AqiLegendItem(aqiValue: Int, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "AQI $aqiValue: $label")
    }
}
