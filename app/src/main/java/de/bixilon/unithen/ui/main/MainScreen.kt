package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.bixilon.unithen.ui.navigation.Navigator


@Composable
fun MainScreen(navigator: Navigator) {

    Row {
        Text("Hi\nPlease login!")
        SitesScreen(navigator)
    }
}
