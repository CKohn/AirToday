package br.com.fiap.airtoday.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import br.com.fiap.airtoday.R
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BemVindoScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var permissionGranted by remember { mutableStateOf(checkLocationPermission(context)) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var locationDenied by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            fetchLocation(context, fusedLocationClient) { location ->
                userLocation = location
                if (location != null) {
                    Log.d("LOCATION_SUCCESS", "Latitude: ${location.latitude}, Longitude: ${location.longitude}")
                    coroutineScope.launch {
                        delay(2000)
                        navController.navigate("dashboard/${location.latitude}/${location.longitude}")
                    }
                }
            }
        } else {
            locationDenied = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val image: Painter = painterResource(id = R.drawable.baseline_air_24)
        Image(
            painter = image,
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.welcome_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.welcome_description),
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (!permissionGranted) {
            Button(
                onClick = {
                    requestLocationPermission(context as Activity) {
                        permissionGranted = true
                        locationDenied = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF025930),
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(id = R.string.allow_location))
            }
        } else if (locationDenied) {
            Button(
                onClick = { locationDenied = false },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text("Sem permissão de localização")
            }
        }
    }
}

fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

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

fun fetchLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location?) -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocationReceived(location)
                } else {
                    Log.e("LOCATION_ERROR", "Última localização não encontrada, solicitando nova...")
                    val locationRequest = LocationRequest.create().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = 1000
                        fastestInterval = 500
                        numUpdates = 1
                    }
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            locationResult.lastLocation?.let {
                                Log.d("LOCATION_SUCCESS", "Nova localização obtida: ${it.latitude}, ${it.longitude}")
                                onLocationReceived(it)
                            } ?: run {
                                Log.e("LOCATION_ERROR", "Erro ao obter nova localização")
                                onLocationReceived(null)
                            }
                        }
                    }
                    val locationProvider = LocationServices.getFusedLocationProviderClient(context)
                    locationProvider.requestLocationUpdates(locationRequest, locationCallback, null)
                }
            }
            .addOnFailureListener {
                Log.e("LOCATION_ERROR", "Erro ao obter última localização: ${it.message}")
                onLocationReceived(null)
            }
    } else {
        Log.e("LOCATION_ERROR", "Permissão de localização negada")
        onLocationReceived(null)
    }
}
