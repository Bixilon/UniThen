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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.R
import de.bixilon.unithen.api.user.SiteDetails
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.storage.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AddSiteProgressDialog(url: String, cancel: () -> Unit, callback: (Site) -> Unit) {
    val storage = LocalStorage.current
    var error: Throwable? by remember { mutableStateOf(null) }

    BackHandler { cancel.invoke() }

    LaunchedEffect(url) {
        try {
            val site = withContext(Dispatchers.IO) { storage.sites.add(url) }
            callback(site)
        } catch (_error: Throwable) {
            _error.printStackTrace()
            error = _error
        }
    }

    AlertDialog(
        onDismissRequest = cancel,
        title = { if (error != null) Text("Error!") else Text("Add Site") },
        text = {
            error?.let {
                Text("An error occurred while fetching page details: $it", color = Color.Red)
                return@AlertDialog
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Fetching page details ($url)...")
            }
        },
        confirmButton = { if (error != null) TextButton(onClick = cancel) { Text("Close") } }
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
        title = { Text("Add New Site") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Enter the URL of the site you want to add:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    state = input,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = { Text("e.g. kurse.uni.de") },
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
                Text("Add Site")
            }
        },
        dismissButton = {
            cancel?.let { TextButton(onClick = it) { Text("Cancel") } }
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
            text = stringResource(R.string.add_account_help),
            modifier = Modifier.padding(vertical = 15.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
