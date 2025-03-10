package br.com.fiap.airtoday.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import br.com.fiap.airtoday.R
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

@Composable
fun BemVindoScreen(navController: NavController) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(checkLocationPermission(context)) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            coroutineScope.launch {
                fetchLocation(context, fusedLocationClient) { location ->
                    userLocation = location
                    location?.let {
                        navController.navigate("dashboard/${it.latitude}/${it.longitude}") {
                            popUpTo("bemVindo") { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(id = R.string.welcome_title))

        if (!permissionGranted) {
            Button(
                onClick = {
                    requestLocationPermission(context as Activity) { granted ->
                        permissionGranted = granted
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.allow_location))
            }
        }
    }
}

fun checkLocationPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun requestLocationPermission(activity: Activity, onPermissionResult: (Boolean) -> Unit) {
    ActivityCompat.requestPermissions(
        activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100
    )

    activity.window.decorView.postDelayed({
        val granted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        onPermissionResult(granted)
    }, 500)
}

fun fetchLocation(context: android.content.Context, fusedLocationClient: FusedLocationProviderClient, onLocationReceived: (Location?) -> Unit) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        onLocationReceived(null)
        return
    }

    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(location)
            } else {
                val locationRequest = LocationRequest.create().apply {
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                    interval = 1000
                    fastestInterval = 500
                    numUpdates = 1
                }

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let {
                            onLocationReceived(it)
                        } ?: run {
                            onLocationReceived(null)
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            }
        }
        .addOnFailureListener {
            onLocationReceived(null)
        }
}
