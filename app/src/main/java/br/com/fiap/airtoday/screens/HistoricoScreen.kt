package br.com.fiap.airtoday.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.airtoday.R
import br.com.fiap.airtoday.model.AirToday
import br.com.fiap.airtoday.repository.AirTodayRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var historico by remember { mutableStateOf<List<AirToday>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val lista = AirTodayRepository.listarHistorico()
            val agora = System.currentTimeMillis()
            val cincoDiasAtras = agora - TimeUnit.DAYS.toMillis(5)
            val listaFiltrada = lista.filter { it.timestamp >= cincoDiasAtras }
            val grupoPorDia = listaFiltrada.groupBy { airToday ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = airToday.timestamp
                }
                val year = cal.get(Calendar.YEAR)
                val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                year to dayOfYear
            }
            val listaUnicaPorDia = grupoPorDia.mapValues { (_, registrosDoDia) ->
                registrosDoDia.maxByOrNull { it.timestamp }
            }.values.filterNotNull()
            val listaFinal = listaUnicaPorDia.sortedByDescending { it.timestamp }
            historico = listaFinal
        } catch (e: Exception) {
            showError = true
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    if (showError) {
                        Text(
                            text = stringResource(id = R.string.history_error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (historico.isEmpty() && !showError) {
                        Text(stringResource(id = R.string.history_empty))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(historico) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "${stringResource(id = R.string.city_label)} ${item.city}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text("AQI: ${item.aqi}")
                                        Text("Data: ${sdf.format(Date(item.timestamp))}")
                                        item.temperature?.let { temp ->
                                            Text("Temp: $temp °C")
                                        }
                                        item.humidity?.let { hum ->
                                            Text("Umidade: $hum%")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(stringResource(id = R.string.clear_history))
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(stringResource(id = R.string.clear_history))
            },
            text = {
                Text(stringResource(id = R.string.clear_history_confirm))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            AirTodayRepository.limparHistorico()
                            historico = emptyList()
                        }
                        showConfirmDialog = false
                    }
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}
