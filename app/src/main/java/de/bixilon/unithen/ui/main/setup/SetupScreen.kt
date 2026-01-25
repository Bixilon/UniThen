package de.bixilon.unithen.ui.main.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.auth.AuthenticationScreen


enum class SetupStage {
    SELECT_SITE,
    LOGIN,
    DONE,
    ;
}

@Composable
fun SelectSiteSetupScreen(callback: ((Site) -> Unit) = {}) {
    val input = remember { TextFieldState("") }
    Row {
        Text("Please select a site where you booked your courses")
        val sites by remember { DataStorage.STORAGE.sites.stateOf { all() } }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sites, key = Site::id) { item ->
                Card(onClick = { callback.invoke(item) }) {
                    Text(item.url.toString())
                }
            }
        }
        TextField(input, placeholder = { Text(text = "kurse.uni.de") })

        Button({ DataStorage.STORAGE.sites.add(input.text.toString().toURI()) }, enabled = input.text.isNotBlank()) {
            Text("Add site")
        }
    }
}

@Composable
fun SetupScreen(callback: () -> Unit) {
    var state by remember { mutableStateOf(SetupStage.SELECT_SITE) }
    var site: Site? by remember { mutableStateOf(null) }
    Text("Setup")

    LaunchedEffect(state) {
        when (state) {
            SetupStage.DONE -> callback.invoke()
            else -> Unit
        }
    }


    when (state) {
        SetupStage.SELECT_SITE -> SelectSiteSetupScreen { site = it; state = SetupStage.LOGIN }
        SetupStage.LOGIN -> AuthenticationScreen(site!!.url) { state = SetupStage.DONE }
        else -> TODO()
    }
}
