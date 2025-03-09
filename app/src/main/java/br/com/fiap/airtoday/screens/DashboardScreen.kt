package br.com.fiap.airtoday.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.fiap.airtoday.R
import br.com.fiap.airtoday.repository.AirTodayRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, latitude: Double, longitude: Double) {
    val coroutineScope = rememberCoroutineScope()
    var airQualityIndex by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dashboardTitle = stringResource(id = R.string.dashboard_title)
    val loadingText = stringResource(id = R.string.loading_data)
    val errorLoadingData = stringResource(id = R.string.error_loading_data)
    val errorFetchingData = stringResource(id = R.string.error_fetching_data)
    val refreshDataText = stringResource(id = R.string.refresh_data)
    val aqiLabel = stringResource(id = R.string.aqi_label)

    fun fetchAirQualityData() {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            errorMessage = null
            try {
                val airToday = AirTodayRepository.listaQualidadesAr(latitude, longitude)
                withContext(Dispatchers.Main) {
                    airQualityIndex = airToday?.aqi
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = errorFetchingData
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
            TopAppBar(title = { Text(dashboardTitle) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Latitude: $latitude")
            Text(text = "Longitude: $longitude")

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Text(loadingText)
            } else {
                errorMessage?.let {
                    Text(it, fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                } ?: airQualityIndex?.let { aqi ->
                    Text(
                        text = "$aqiLabel $aqi",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { fetchAirQualityData() }
            ) {
                Text(refreshDataText)
            }
        }
    }
}
