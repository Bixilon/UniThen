package de.bixilon.unithen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.auth.AUTHENTICATION_ROUTE
import de.bixilon.unithen.ui.auth.AuthenticationScreen
import de.bixilon.unithen.ui.main.*
import de.bixilon.unithen.ui.main.appointment.APPOINTMENTS_ROUTE
import de.bixilon.unithen.ui.main.appointment.AppointmentsScreen

@Composable
fun NavigationStack() {
    val navigation = rememberNavController()

    NavHost(navController = navigation, startDestination = MAIN_ROUTE) {
        composable(route = MAIN_ROUTE) { MainScreen(navigation) }

        composable(route = APPOINTMENTS_ROUTE) { AppointmentsScreen(navigation) }
        composable(route = COURSES_ROUTE) { CoursesScreen(navigation) }
        composable(route = SITES_ROUTE) { SitesScreen(navigation) }

        composable(route = AUTHENTICATION_ROUTE) {
            val site = DataStorage.STORAGE.getSite(it.arguments!!.getInt("site"))
            AuthenticationScreen(site.url)
        }
    }
}
