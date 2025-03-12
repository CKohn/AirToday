package br.com.fiap.airtoday.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(navController: NavController) {
    val context = LocalContext.current

    // Estado para guardar o número de emergência detectado
    var phoneNumber by remember { mutableStateOf("112") } // Fallback padrão se não acharmos o país
    val coroutineScope = rememberCoroutineScope()

    // Buscamos a localização e definimos o número de emergência assim que a tela abre
    LaunchedEffect(Unit) {
        // Verifica se a permissão de localização está concedida
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        // Obtém a localização mais recente
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        val location = fusedLocationClient.lastLocation.await()
                            ?: run {
                                Log.e("TipsScreen", "location == null, usando fallback 112")
                                return@withContext
                            }

                        // Descobre o país via Geocoder
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val countryCode = addresses?.firstOrNull()?.countryCode // ex.: "BR", "US" etc.

                        // Mapeamento de códigos de país -> número de emergência
                        val emergencyMap = mapOf(
                            "BR" to "192", // SAMU no Brasil
                            "US" to "911",
                            "GB" to "999",
                            "FR" to "112",
                            "ES" to "112",
                            "PT" to "112",
                            // Adicione mais conforme necessidade
                        )

                        val detectedNumber = emergencyMap[countryCode] ?: "112"
                        phoneNumber = detectedNumber

                        Log.d("TipsScreen", "País detectado: $countryCode -> número emergência: $detectedNumber")

                    } catch (e: Exception) {
                        Log.e("TipsScreen", "Erro ao obter localização/país: ${e.message}")
                    }
                }
            }
        } else {
            Log.e("TipsScreen", "Sem permissão de localização. Usando fallback 112.")
        }
    }

    // Conteúdo da tela
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Air Quality Tips") },
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
            // Área de dicas rolável
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "How to Stay Safe During Poor Air Quality:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("• Check the AQI daily before planning outdoor activities.")
                Text("• Keep windows and doors closed when pollution is high.")
                Text("• Use an air purifier indoors if possible.")
                Text("• Stay hydrated and maintain a healthy diet.")
                Text("• Consider wearing an N95 mask if AQI is Very Poor.")

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Eco-Friendly Habits:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("• Use public transport or carpool to reduce emissions.")
                Text("• Turn off lights and electronics when not in use.")
                Text("• Recycle and dispose of waste responsibly.")
                Text("• Plant trees or maintain indoor plants to help purify the air.")
                Text("• Avoid burning trash or leaves, which contributes to air pollution.")

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tips for Sensitive Groups:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("• Children, the elderly, and those with respiratory issues should limit outdoor activities when AQI is high.")
                Text("• Keep medications (like inhalers) easily accessible if you have asthma.")
                Text("• Consult your doctor for specific guidance if you have chronic conditions.")

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "When to Seek Medical Help:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("• If you experience shortness of breath, wheezing, or severe coughing.")
                Text("• If you have chest pain or tightness that doesn’t improve.")
                Text("• If your symptoms worsen despite taking usual medications.")
            }

            // Rodapé com botões
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botão "Got it!"
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Got it!")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botão "Call Emergency"
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:$phoneNumber")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Call Emergency")
                }
            }
        }
    }
}
