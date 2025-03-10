package br.com.fiap.airtoday.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = Modifier
    ) {
        composable(
            route = "welcome",
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            BemVindoScreen(navController)
        }

        composable(
            route = "dashboard/{latitude}/{longitude}",
            arguments = listOf(
                navArgument("latitude") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("longitude") { type = NavType.StringType; defaultValue = "0.0" }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(500)) +
                        fadeIn(animationSpec = tween(500))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(500)) +
                        fadeOut(animationSpec = tween(500))
            }
        ) { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0
            DashboardScreen(navController, latitude, longitude)
        }

        composable("historico") {
            HistoricoScreen(navController)
        }

    }
}
