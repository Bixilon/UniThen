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

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.auth.AuthenticationScreen


fun ByteArray.toBitmap() = BitmapFactory.decodeByteArray(this, 0, this.size)

@Composable
private fun SiteList(sites: List<Site>, callback: (Site) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = sites,
            key = Site::id
        ) { site ->
            Card(modifier = Modifier.fillMaxWidth(), onClick = { callback.invoke(site) }) {
                val bitmap = remember { site.icon?.toBitmap()?.asImageBitmap() }
                if (bitmap != null) {
                    Image(bitmap = bitmap, contentDescription = "icon")
                }
                Text(text = site.name)
                Text(text = site.url.toString())
            }
        }
    }
}

@Composable
fun SelectSiteSetupScreen(callback: (Site) -> Unit = {}) {
    val sites by remember { DataStorage.STORAGE.sites.stateOf { all() } }

    if (sites.isEmpty()) {
        AddSiteDialog(callback)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Please select a site where you booked your courses")

        SiteList(sites, callback)
        AddSiteButton(callback)
    }
}


@Composable
fun AddAccountScreen(callback: () -> Unit) {
    var site: Site? by remember { mutableStateOf(null) }

    BackHandler(site != null) { site = null }

    site?.let {
        AuthenticationScreen(it) { callback.invoke() }
        return
    }

    SelectSiteSetupScreen { site = it }
}
