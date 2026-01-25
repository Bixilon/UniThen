package de.bixilon.unithen.ui

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
import de.bixilon.unithen.ui.auth.AuthenticationScreen
import de.bixilon.unithen.ui.main.*
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.theme.UniThenTheme

@Composable
fun MainNavigator() {
    val navigation = remember { Navigator(Home) }

    navigation.routes {
        composable<Home> { MainScreen(navigation) }

        // composable(route = APPOINTMENTS_ROUTE) { AppointmentsScreen(navigation) }
        // composable(route = COURSES_ROUTE) { CoursesScreen(navigation) }
        // composable(route = "/course/{course}") {CourseScreen(course, navigation)}

        composable<Sites> { SitesScreen(navigation) }

        composable<AuthRoute> { AuthenticationScreen(it.site.url) }
    }

    navigation.Host()
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                        MainNavigator()
                    }
                }
            }
        }
    }
}
