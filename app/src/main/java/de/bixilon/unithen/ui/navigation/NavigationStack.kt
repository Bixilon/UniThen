package de.bixilon.unithen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.bixilon.unithen.ui.auth.AUTHENTICATION_ROUTE
import de.bixilon.unithen.ui.auth.AuthenticationScreen
import de.bixilon.unithen.ui.main.MAIN_ROUTE
import de.bixilon.unithen.ui.main.MainScreen

@Composable
fun NavigationStack() {
    val navigation = rememberNavController()

    NavHost(navController = navigation, startDestination = MAIN_ROUTE) {
        composable(route = MAIN_ROUTE) { MainScreen(navigation) }
        composable(route = AUTHENTICATION_ROUTE) { AuthenticationScreen() }
    }
}
