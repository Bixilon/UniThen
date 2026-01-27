package de.bixilon.unithen.ui.main.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.api.user.SiteDetails
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site


@Composable
private fun AddSiteProgressDialog(url: String, callback: (Site) -> Unit) {
    var text: String by remember { mutableStateOf("Fetching: $url") }

    LaunchedEffect(Unit) {
        DefaultThreadPool += {
            try {
                val url = SiteDetails.fix(url)
                val details = SiteDetails.fetch(url)

                val site = DataStorage.STORAGE.sites.add(url, details.name, details.icon)
                text = "Done!"
                callback.invoke(site)
            } catch (error: Throwable) {
                error.printStackTrace()
                text = error.toString()
            }
        }
    }

    AlertDialog({}, {}, text = { Text(text) })
}

@Composable
fun AddSiteDialog(callback: (Site) -> Unit) {
    val input = remember { TextFieldState("") }
    var url: String? by remember { mutableStateOf(null) }

    if (url != null) {
        AddSiteProgressDialog(url!!, callback)
        return
    }


    LaunchedEffect(input) {
        val text = input.text.toString()
        val fixed = catchAll { SiteDetails.fix(text).toString() } ?: text
        if (fixed != text) {
            input.edit { this.replace(0, this.length, fixed) }
        }
    }


    Column {

        TextField(state = input, modifier = Modifier.fillMaxWidth(), placeholder = { Text("kurse.uni.de") })
        Button(
            onClick = {
                val text = input.text.toString()
                input.clearText()
                url = text
            },
            enabled = input.text.isNotBlank()
        ) {
            Text("Add site")
        }
    }
}


@Composable
fun AddSiteButton(callback: (Site) -> Unit) {
    var open by remember { mutableStateOf(false) }

    if (open) {
        AddSiteDialog(callback)
    }

    if (!open) {
        Button({ open = true }) {
            Text("Can not find your site?")
        }
    }
}
