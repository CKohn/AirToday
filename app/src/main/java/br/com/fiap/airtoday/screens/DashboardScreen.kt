package br.com.fiap.airtoday.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
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
    var lastUpdate by remember { mutableStateOf<String>("---") }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    fun fetchAirQualityData() {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            hasError = false
            try {
                val lastLocation = fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                lastLocation?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                }

                val airToday = AirTodayRepository.listaQualidadesAr(latitude, longitude)

                withContext(Dispatchers.Main) {
                    cityName = airToday?.city
                    airQualityIndex = airToday?.aqi
                    temperature = airToday?.temperature
                    humidity = airToday?.humidity
                    lastUpdate = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
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

    LaunchedEffect(Unit) {
        fetchAirQualityData()
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
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(animationSpec = tween(500)) + scaleIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
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
                            text = "${stringResource(id = R.string.city_label)} ${cityName ?: stringResource(id = R.string.unknown_location)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(id = R.string.latitude_longitude_label, latitude, longitude),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (hasError) {
                            Text(
                                text = stringResource(id = R.string.error_loading_data),
                                fontSize = 18.sp,
                                color = Color.Red
                            )
                        } else {
                            airQualityIndex?.let { aqi ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = when (aqi) {
                                                    1 -> listOf(Color(0xFF00E400), Color(0xFF008000)) // Verde
                                                    2 -> listOf(Color(0xFFFFFF00), Color(0xFFCCCC00)) // Amarelo
                                                    3 -> listOf(Color(0xFFFFA500), Color(0xFFD2691E)) // Laranja
                                                    4 -> listOf(Color(0xFFFF0000), Color(0xFF8B0000)) // Vermelho
                                                    5 -> listOf(Color(0xFF800080), Color(0xFF4B0082)) // Roxo
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { fetchAirQualityData() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF025930))
            ) {
                Text(stringResource(id = R.string.refresh_data))
            }
        }
    }
}
