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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.user.SiteDetails
import de.bixilon.unithen.storage.sql.DummyStorage.initializeDummy
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.main.MainRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.BackHandler
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.state.rememberStateOf
import de.bixilon.unithen.ui.util.useAsyncNetwork
import de.bixilon.unithen.ui.util.useToast
import unithen.common.generated.resources.*

@Composable
fun AddSiteProgressDialog(url: String, cancel: () -> Unit, callback: (Site) -> Unit) {
    val navigation = LocalNavigation.current
    val toast = useToast()
    val storage = LocalStorage.current

    useAsyncNetwork(true) {
        try {
            if (url == "dummy.local") {
                storage.initializeDummy()
                toast.invoke("Dummy database was loaded!")
                navigation.popIf { it !is MainRoute }
                return@useAsyncNetwork
            }
            val site = storage.sites.add(url)
            callback.invoke(site)
        } catch (error: Throwable) {
            cancel.invoke()
            throw error
        }
    }


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
    var text by rememberStateOf { "" }
    var url: String? by remember { mutableStateOf(null) }

    url?.let {
        AddSiteProgressDialog(it, { url = null }, callback)
        return
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
                    value = text,
                    onValueChange = { text = SiteDetails.fix(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri
                    ),
                    placeholder = { Text(Res.string.sites_add_placeholder.i18n()) },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    url = text
                    text = ""
                },
                enabled = "." in text && text.isNotBlank(),
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
