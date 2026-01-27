package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.navigation.LocalNavigation


@Composable
fun MainScreen() {
    val navigator = LocalNavigation.current

    LaunchedEffect(Unit) {
        if (DataStorage.STORAGE.accounts.count == 0) {
            navigator.navigate(SetupRoute)
        }
    }


    Column {
        Text("Welcome!")
        Button({ navigator.navigate(SetupRoute) }) { Text("Open setup") }

        SitesScreen()
    }
}
