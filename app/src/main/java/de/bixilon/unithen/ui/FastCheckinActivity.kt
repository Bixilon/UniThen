package de.bixilon.unithen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.bixilon.unithen.ui.main.FAST_CHECK_IN_ROUTE
import de.bixilon.unithen.ui.main.FastCheckInInScreen
import de.bixilon.unithen.ui.theme.UniThenTheme

class FastCheckinActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniThenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val navigation = rememberNavController()

                    NavHost(navController = navigation, startDestination = FAST_CHECK_IN_ROUTE) {
                        composable(route = FAST_CHECK_IN_ROUTE) { FastCheckInInScreen() }
                    }
                }
            }
        }
    }
}
