package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.navigation.LocalNavigation


@Composable
fun SitesScreen() {
    val sites by remember { DataStorage.STORAGE.sites.stateOf { all() } }

    Row {
        Text("Sites")


        LazyColumn(Modifier.fillMaxSize()) {
            items(sites, key = Site::id) {
                Column {
                    Text(it.url.toString())

                    val navigator = LocalNavigation.current
                    Button({ navigator.navigate(AuthenticationRoute(it)) }) {
                        Text("Authenticate")
                    }
                }
            }
        }

    }
}
