package de.bixilon.unithen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.auth.AuthenticationScreen
import de.bixilon.unithen.ui.main.MAIN_ROUTE
import de.bixilon.unithen.ui.main.MainScreen
import de.bixilon.unithen.ui.main.SITES_ROUTE
import de.bixilon.unithen.ui.main.SitesScreen
import de.bixilon.unithen.ui.main.appointment.APPOINTMENTS_ROUTE
import de.bixilon.unithen.ui.main.appointment.AppointmentsScreen
import de.bixilon.unithen.ui.main.course.COURSES_ROUTE
import de.bixilon.unithen.ui.main.course.CourseScreen
import de.bixilon.unithen.ui.main.course.CoursesScreen

@Composable
fun NavigationStack() {
    val navigation = rememberNavController()

    NavHost(navController = navigation, startDestination = MAIN_ROUTE) {
        composable(route = MAIN_ROUTE) { MainScreen(navigation) }

        composable(route = APPOINTMENTS_ROUTE) { AppointmentsScreen(navigation) }
        composable(route = COURSES_ROUTE) { CoursesScreen(navigation) }
        composable(route = "/course/{course}") {
            val course = DataStorage.STORAGE.courses[it.arguments!!.getString("course")!!.toInt()]!!
            CourseScreen(course, navigation)
        }

        composable(route = SITES_ROUTE) { SitesScreen(navigation) }

        composable(route = "/auth/{site}") {
            val site = DataStorage.STORAGE.sites[it.arguments!!.getString("site")!!.toInt()]!!
            AuthenticationScreen(site.url)
        }
    }
}
