package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun MainScreen() {

    Row {
        Text("Hi\nPlease login!")
        SitesScreen()
    }
}
