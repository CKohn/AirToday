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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.com.fiap.airtoday.model.AirToday
import br.com.fiap.airtoday.repository.AirTodayRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var historico by remember { mutableStateOf<List<AirToday>>(emptyList()) }

    // Carrega os dados ao abrir a tela
    LaunchedEffect(Unit) {
        historico = AirTodayRepository.listarHistorico()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Qualidade do Ar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // 🔹 Aqui está a correção!
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (historico.isEmpty()) {
                Text("Nenhum dado salvo ainda.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(historico) { item ->
                        HistoricoItem(item)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        AirTodayRepository.limparHistorico()
                        historico = emptyList()
                    }
                }
            ) {
                Text("Limpar Histórico")
            }
        }
    }
}

@Composable
fun HistoricoItem(item: AirToday) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Cidade: ${item.city}", style = MaterialTheme.typography.bodyLarge)
            Text("AQI: ${item.aqi}", style = MaterialTheme.typography.bodyMedium)
            Text("Temperatura: ${item.temperature ?: "N/A"}°C", style = MaterialTheme.typography.bodyMedium)
            Text("Umidade: ${item.humidity ?: "N/A"}%", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
