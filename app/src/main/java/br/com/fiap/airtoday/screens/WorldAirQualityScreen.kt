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
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Dados básicos de cada capital (nome, lat, lon)
data class WorldCity(
    val cityName: String,
    val latitude: Double,
    val longitude: Double
)

// Dados de qualidade do ar retornados (podemos expandir conforme necessário)
data class CityAirQuality(
    val cityName: String,
    val aqi: Int?,
    val temperature: Double?,
    val humidity: Int?,
    val lastUpdate: String
)

// Lista fixa de capitais para demonstrar
private val capitals = listOf(
    WorldCity("New York", 40.7128, -74.0060),
    WorldCity("London", 51.5074, -0.1278),
    WorldCity("Tokyo", 35.6895, 139.6917),
    WorldCity("Brasília", -15.7939, -47.8828),
    WorldCity("Paris", 48.8566, 2.3522)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldAirQualityScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()

    // Armazena a lista de dados carregados para cada cidade
    var worldData by remember { mutableStateOf<List<CityAirQuality>>(emptyList()) }
    // Controla loading e erro
    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    // Formato de data/hora
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    // Função para buscar dados de todas as capitais
    fun fetchAllCapitals() {
        coroutineScope.launch(Dispatchers.IO) {
            isLoading = true
            showError = false
            val resultList = mutableListOf<CityAirQuality>()

            try {
                // Para cada capital, chamamos o repositório e montamos o objeto de dados
                for (city in capitals) {
                    val airToday = AirTodayRepository.listaQualidadesAr(city.latitude, city.longitude)
                    // Se não houve erro, adiciona à lista
                    if (airToday != null) {
                        val item = CityAirQuality(
                            cityName = city.cityName,
                            aqi = airToday.aqi,
                            temperature = airToday.temperature,
                            humidity = airToday.humidity,
                            lastUpdate = dateFormat.format(Date())
                        )
                        resultList.add(item)
                    } else {
                        // Se airToday vier nulo para alguma cidade, podemos tratar como erro local
                        // mas aqui apenas não adicionamos
                    }
                }
                // Atualiza o estado com a lista final
                withContext(Dispatchers.Main) {
                    worldData = resultList
                    isLoading = false
                }
            } catch (e: Exception) {
                // Se der erro geral, marcamos showError
                withContext(Dispatchers.Main) {
                    showError = true
                    isLoading = false
                }
            }
        }
    }

    // Carrega ao entrar na tela
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
        // Conteúdo principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Se estiver carregando, mostra loading
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Se deu erro, mostra mensagem
            else if (showError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Error loading data.")
                }
            }
            // Caso contrário, exibe a lista de capitais
            else {
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

/**
 * Exibe um Card semelhante ao da Dashboard, mas para cada cidade.
 */
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

            // AQI
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

            // Temperatura e Umidade
            Text("Temperature: %.1f °C".format(cityData.temperature ?: 0.0), fontSize = 16.sp)
            Text("Humidity: %d %%".format(cityData.humidity ?: 0), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))

            // Última atualização
            Text("Last update: ${cityData.lastUpdate}", fontSize = 12.sp)
        }
    }
}
