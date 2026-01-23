package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import de.bixilon.unithen.ui.auth.AUTHENTICATION_ROUTE


const val MAIN_ROUTE = "/main"
@Composable
fun MainScreen(navigation: NavController) {

    Row {
        Text("Hi\nPlease login!")
        Button({ navigation.navigate(AUTHENTICATION_ROUTE) }) {
            Text("Authenticate")
        }
    }
}
