package br.com.fiap.airtoday.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
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

@Composable
fun BemVindoScreen(navController: NavController) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(checkLocationPermission(context)) }
    var userLocation by remember { mutableStateOf<Location?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        permissionGranted = checkLocationPermission(context)
    }

    fun fetchLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location
                }
            }.addOnFailureListener {
                userLocation = null
            }
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
                        fetchLocation()
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
        } else {
            Button(
                onClick = {
                    fetchLocation()
                    userLocation?.let {
                        navController.navigate("dashboard/${it.latitude}/${it.longitude}")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF025930),
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(id = R.string.go_to_dashboard))
            }
        }
    }
}

/**
 * Verifica se a permissão de localização foi concedida.
 */
fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Solicita a permissão de localização ao usuário.
 */
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
