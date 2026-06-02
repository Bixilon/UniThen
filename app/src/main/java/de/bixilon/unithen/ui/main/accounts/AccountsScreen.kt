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

package de.bixilon.unithen.ui.main.accounts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.time.DurationUtil.weeks
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetch
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.AccountDetailsRoute
import de.bixilon.unithen.ui.main.AddAccountRoute
import de.bixilon.unithen.ui.main.add.toBitmap
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.useAsyncNetwork
import de.bixilon.unithen.ui.util.verticalScroll
import kotlin.time.Clock


@Composable
private fun AccountOptions(account: Account, modifier: Modifier) {
    val storage = LocalStorage.current
    var expanded by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val update = useAsyncNetwork<Unit>(account) { storage.fetch(account, true) }
            DropdownMenuItem(
                text = { Text(if (refreshing) "Updating..." else "Update") },
                enabled = !refreshing,
                onClick = {
                    refreshing = true
                    update.invoke(Unit)
                    expanded = false
                }
            )
            // TODO: Remove account
        }
    }


    if (refreshing) {
        AlertDialog(
            confirmButton = {},
            onDismissRequest = {},
            title = { Text("Refreshing account...") },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fetching courses...")
                }
            },
        )
    }
}

@Composable
private fun AccountCard(account: Account, onClick: () -> Unit) {
    val site = rememberStorage { sites[account.site]!! }


    val color = when {
        account.session.isNullOrBlank() -> MaterialTheme.colorScheme.errorContainer
        Clock.System.now() - account.fetched < 4.weeks -> MaterialTheme.colorScheme.primaryContainer // TODO: That color sucks
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = account.firstname + " " + account.lastname,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            AccountOptions(account, Modifier
                .align(Alignment.TopEnd)
                .offset(16.dp, -16.dp) // TODO: why?
            )


            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) {
                val bitmap = remember(site.icon) { site.icon?.toBitmap()?.asImageBitmap() }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Site icon",
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = site.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}


@Composable
fun AccountsScreen() {
    val accounts = rememberStorage { accounts.all() }

    Screen {
        ScreenTitle("Accounts (${accounts.size})")

        Box {
            val navigator = LocalNavigation.current
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state),
                state = state,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = accounts, key = Account::id) { account -> AccountCard(account) { navigator.navigate(AccountDetailsRoute(account)) } }
            }

            FloatingActionButton({ navigator.navigate(AddAccountRoute) }, modifier = Modifier
                .align(Alignment.BottomEnd)) {
                Icon(Icons.Filled.Add, "add")
            }
        }
    }
}
