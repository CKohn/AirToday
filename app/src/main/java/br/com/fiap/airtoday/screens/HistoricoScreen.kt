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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var historico by remember { mutableStateOf<List<AirToday>>(emptyList()) }

    LaunchedEffect(Unit) {
        historico = AirTodayRepository.listarHistorico()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (historico.isEmpty()) {
                Text(stringResource(id = R.string.history_empty))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(historico) { item ->
                        Text("${stringResource(id = R.string.city_label)} ${item.city}")
                    }
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        AirTodayRepository.limparHistorico()
                        historico = emptyList()
                    }
                }
            ) {
                Text(stringResource(id = R.string.clear_history))
            }
        }
    }
}
