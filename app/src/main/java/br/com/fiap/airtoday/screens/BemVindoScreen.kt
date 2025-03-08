package br.com.fiap.airtoday.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import br.com.fiap.airtoday.R
import com.google.android.gms.location.*

@Composable
fun BemVindoScreen(navController: NavController) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(checkLocationPermission(context)) }
    var location by remember { mutableStateOf<Location?>(null) }

    // Gerenciador de localização
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        permissionGranted = checkLocationPermission(context)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val image: Painter = painterResource(id = R.drawable.baseline_air_24)

        Image(
            painter = image,
            contentDescription = "Ícone do app",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bem-vindo ao AirToday",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Monitore a qualidade do ar em tempo real e cuide da sua saúde!",
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!permissionGranted) {
                    requestLocationPermission(context as Activity) {
                        permissionGranted = true
                    }
                } else {
                    getLastKnownLocation(context, fusedLocationClient) { loc ->
                        location = loc
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (permissionGranted) "Obter Localização" else "Permitir Localização")
        }

        // Exibir a localização obtida
        location?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Sua localização: ${it.latitude}, ${it.longitude}")
        }
    }
}

// Verifica se a permissão de localização já foi concedida
fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// Solicita a permissão de localização ao usuário
fun requestLocationPermission(activity: Activity, onPermissionGranted: () -> Unit) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        100
    )

    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
        onPermissionGranted()
    }
}

// Obtém a última localização conhecida do usuário
fun getLastKnownLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location)
                } else {
                    // Se a última localização for nula, solicitar uma nova atualização
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY, 1000
                    ).setMinUpdateIntervalMillis(500).build()

                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            fusedLocationClient.removeLocationUpdates(this)
                            onLocationReceived(locationResult.lastLocation)
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            }
            .addOnFailureListener { exception ->
                println("Erro ao obter localização: ${exception.message}")
                onLocationReceived(null) // Retorna null se falhar
            }
    } else {
        println("Permissão de localização não concedida.")
    }
}

