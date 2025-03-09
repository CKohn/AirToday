package br.com.fiap.airtoday.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = Modifier
    ) {
        composable("welcome") { BemVindoScreen(navController) }
//        composable("home") {  }
        composable("dashboard") {
            DashboardScreen(navController)
        }
    }
}
