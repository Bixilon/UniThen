package de.bixilon.unithen.ui.main.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.unithen.api.user.SiteDetails
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
fun SelectSiteSetupScreen(
    onSiteSelected: (Site) -> Unit = {},
) {
    val input = remember { TextFieldState("") }
    val sites by remember { DataStorage.STORAGE.sites.stateOf { all() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Please select a site where you booked your courses")

        LazyColumn(
            modifier = Modifier.weight(1.0f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = sites,
                key = Site::id
            ) { site ->
                Card(modifier = Modifier.fillMaxWidth(), onClick = { onSiteSelected(site) }) {
                    Text(text = site.name)
                    Text(text = site.url.toString())
                }
            }
        }

        TextField(state = input, modifier = Modifier.fillMaxWidth(), placeholder = { Text("kurse.uni.de") })

        var text: String? by remember { mutableStateOf(null) }

        if (text != null) {
            AlertDialog({}, {}, text = { Text(text ?: "") })
        }

        Button(
            onClick = {
                val url = input.text.toString()
                input.clearText()
                text = "Fetching: $url"
                DefaultThreadPool += {
                    try {
                        val url = SiteDetails.fix(url)
                        val details = SiteDetails.fetch(url)

                        val site = DataStorage.STORAGE.sites.add(url, details.name, details.icon)
                        text = "Done!"
                        onSiteSelected.invoke(site)
                    } catch (error: Throwable) {
                        error.printStackTrace()
                        text = error.toString()
                    }
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = input.text.isNotBlank()
        ) {
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
