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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.R
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.auth.AuthenticationScreen
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.i18n


fun ByteArray.toBitmap() = BitmapFactory.decodeByteArray(this, 0, this.size)

@Composable
private fun SiteCard(site: Site, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row {
                val bitmap = remember(site.icon) { site.icon?.toBitmap()?.asImageBitmap() }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Site icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))
                }

                Text(
                    text = site.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = site.host,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            )
        }
    }
}


@Composable
fun SelectSiteSetupScreen(callback: (Site) -> Unit = {}) {
    val sites = rememberStorage { sites.all() }

    if (sites.isEmpty()) {
        AddSiteDialog(null, callback)
        return
    }

    Screen {
        ScreenTitle(R.string.add_account_title.i18n())

        LazyColumn(modifier = Modifier.weight(1.0f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items = sites, key = Site::id) { site -> SiteCard(site, Modifier.clickable { callback.invoke(site) }) }
        }

        Spacer(Modifier.height(16.dp))

        AddSiteButton(callback) // TODO: FloatingActionButton?
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
