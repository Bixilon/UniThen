package de.bixilon.unithen.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.main.CheckInScreen
import de.bixilon.unithen.ui.main.FastCheckInInScreen
import de.bixilon.unithen.ui.main.FastCheckinAppointment
import de.bixilon.unithen.ui.theme.UniThenTheme


class FastCheckinActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (DataStorage.STORAGE.appointments.count == 0) {
            this.startActivity(Intent(this, MainActivity::class.java))
            return
        }

        setContent {
            UniThenTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val navigation = rememberNavController()

                        NavHost(navController = navigation, startDestination = "/") {
                            composable(route = "/") { FastCheckInInScreen(navigation) }
                            composable(route = "/appointment/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { FastCheckinAppointment(navigation, DataStorage.STORAGE.appointments[it.arguments!!.getInt("id")]!!) }
                            composable(route = "/appointment/{id}/{account}", arguments = listOf(navArgument("id") { type = NavType.IntType }, navArgument("account") { type = NavType.IntType })) {
                                val appointment = DataStorage.STORAGE.appointments[it.arguments!!.getInt("id")]!!
                                val course = DataStorage.STORAGE.courses[appointment.course]!!
                                val account = DataStorage.STORAGE.accounts[it.arguments!!.getInt("account")]!!
                                CheckInScreen(account, course, appointment)
                            }
                        }
                    }
                }
            }
        }
    }
}
