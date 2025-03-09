package br.com.fiap.airtoday.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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

    /**
     * Obtém os dados da API.
     */
    fun fetchAirQualityData() {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            hasError = false
            try {
                // 🔹 Obtém a localização mais recente antes de buscar os dados da API
                val lastLocation = fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                lastLocation?.let {
                    latitude = it.latitude
                    longitude = it.longitude
                }

                // 🔹 Agora busca os dados atualizados da API
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


    /**
     * Obtém a localização do usuário.
     */
    @SuppressLint("MissingPermission")
    fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    fetchAirQualityData() // Atualiza os dados após obter a localização
                } else {
                    println("Erro ao obter localização atual.")
                }
            }
        } else {
            println("Permissão de localização não concedida!")
        }
    }

    // Obtém os dados assim que a tela carregar
    LaunchedEffect(Unit) {
        fetchAirQualityData()
    }

    /**
     * Interface gráfica do Dashboard.
     */
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

                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (hasError) {
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
                                        when (aqi) {
                                            1 -> Color(0xFF00E400) // Verde - Boa
                                            2 -> Color(0xFFFFFF00) // Amarelo - Moderada
                                            3 -> Color(0xFFFFA500) // Laranja - Ruim
                                            4 -> Color(0xFFFF0000) // Vermelho - Muito Ruim
                                            5 -> Color(0xFF800080) // Roxo - Perigoso
                                            else -> Color.Gray
                                        },
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
