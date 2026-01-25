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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.main.CheckInScreen
import de.bixilon.unithen.ui.main.fast.*
import de.bixilon.unithen.ui.navigation.UnserializedNavigation
import de.bixilon.unithen.ui.theme.UniThenTheme


@Composable
fun FastCheckInNavigator() {
    val navigation = rememberNavController()
    val shit = remember { UnserializedNavigation(FastCheckinHome, navigation) }

    NavHost(navController = navigation, startDestination = "/") {
        shit.host = this
        composable(route = "/") { FastCheckInInScreen(shit) }
        shit.composable3<FastCheckinHome> { FastCheckInInScreen(shit) }

        shit.composable3<CheckInAppointment> { FastCheckinAppointment(shit, it.course, it.appointment) }
        shit.composable3<CheckInRoute> { CheckInScreen(it.account, it.course, it.appointment) }
    }

    shit.navigate(FastCheckinHome)
}

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
                        FastCheckInNavigator()
                    }
                }
            }
        }
    }
}
