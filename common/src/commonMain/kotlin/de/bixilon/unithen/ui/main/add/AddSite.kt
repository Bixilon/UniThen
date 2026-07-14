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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.api.user.SiteDetails
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useAsyncNetwork
import unithen.common.generated.resources.*

@Composable
fun AddSiteProgressDialog(url: String, cancel: () -> Unit, callback: (Site) -> Unit) {
    val storage = LocalStorage.current

    val add = useAsyncNetwork<Unit>(null) {
        try {
            val site = storage.sites.add(url)
            callback.invoke(site)
        } catch (error: Throwable) {
            cancel.invoke()
            throw error
        }
    }

    LaunchedEffect(url) { add.invoke(Unit) }


    AlertDialog(
        onDismissRequest = cancel,
        title = { Text(Res.string.sites_fetching_title.i18n()) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(Res.string.sites_fetching_description.i18n())
            }
        },
        confirmButton = { Button(onClick = cancel) { Text(Res.string.cancel.i18n()) } }
    )
}

@Composable
fun AddSiteDialog(cancel: (() -> Unit)?, callback: (Site) -> Unit) {
    var url: String? by remember { mutableStateOf(null) }

    url?.let {
        AddSiteProgressDialog(it, { url = null }, callback)
        return
    }

    val input = remember { TextFieldState("") }

    LaunchedEffect(input.text) {
        val text = input.text.toString()
        val fixed = catchAll { SiteDetails.fix(text) } ?: text
        if (fixed != text) {
            input.edit { this.replace(0, this.length, fixed) }
        }
    }

    AlertDialog(
        onDismissRequest = cancel ?: {},
        title = { Text(Res.string.sites_add_title.i18n()) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = Res.string.sites_add_description.i18n(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    state = input,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = { Text(Res.string.sites_add_placeholder.i18n()) },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val text = input.text.toString()
                    input.clearText()
                    url = text
                },
                enabled = catchAll { SiteDetails.fix(input.text.toString()) }?.isNotBlank() ?: false,
            ) {
                Icon(Icons.Filled.Add, "add")
                Spacer(Modifier.width(8.dp))
                Text(Res.string.sites_add_add.i18n())
            }
        },
        dismissButton = {
            cancel?.let { TextButton(onClick = it) { Text(Res.string.cancel.i18n()) } }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}


@Composable
fun AddSiteButton(callback: (Site) -> Unit) {
    var open by remember { mutableStateOf(false) }


    if (open) {
        BackHandler { open = false }
        AddSiteDialog({ open = false }, callback)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clickable { open = true },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = Res.string.add_account_help.i18n(),
            modifier = Modifier.padding(vertical = 15.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
