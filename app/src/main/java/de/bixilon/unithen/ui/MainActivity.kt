package de.bixilon.unithen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.bixilon.unithen.ui.auth.AuthenticationScreen
import de.bixilon.unithen.ui.main.*
import de.bixilon.unithen.ui.main.add.AddAccountScreen
import de.bixilon.unithen.ui.main.setup.SetupScreen
import de.bixilon.unithen.ui.navigation.Navigator
import de.bixilon.unithen.ui.theme.UniThenTheme


@Composable
fun MainNavigator() {
    val navigator = remember { Navigator(HomeRoute) }

    navigator.routes {
        composable<HomeRoute> { MainScreen() }
        composable<SetupRoute> { SetupScreen() }

        // composable(route = APPOINTMENTS_ROUTE) { AppointmentsScreen(navigation) }
        // composable(route = COURSES_ROUTE) { CoursesScreen(navigation) }
        // composable(route = "/course/{course}") {CourseScreen(course, navigation)}

        composable<SitesRoute> { SitesScreen() }

        composable<AddAccountRoute> { AddAccountScreen { navigator.pop() } }
        composable<AuthenticationRoute> { AuthenticationScreen(it.site.url) { navigator.pop() } }
    }

    navigator.Host()
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UniThenTheme {
                Scaffold(
                    modifier = Modifier.imePadding(),
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
