package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController


const val MAIN_ROUTE = "/main"

@Composable
fun MainScreen(navigation: NavController) {

    Row {
        Text("Hi\nPlease login!")
        SitesScreen(navigation)
    }
}
