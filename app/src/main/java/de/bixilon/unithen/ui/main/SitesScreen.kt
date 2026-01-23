package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController


const val SITES_ROUTE = "/sites"

@Composable
fun SitesScreen(navigation: NavController) {

    Row {
        Text("Sites")
    }
}
