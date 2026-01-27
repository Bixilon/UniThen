/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.ui.main.add

import androidx.activity.compose.BackHandler
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

    BackHandler(url != null) { url = null }

    url?.let {
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
