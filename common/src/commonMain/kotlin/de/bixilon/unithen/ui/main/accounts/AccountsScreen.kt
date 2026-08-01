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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.time.weeks
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.ui.containers.FloatingActionButtons
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.AccountDetailsRoute
import de.bixilon.unithen.ui.main.AddAccountRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.storage.rememberStorageAsync
import de.bixilon.unithen.ui.sync.status.SyncStatusDialog
import de.bixilon.unithen.ui.sync.useSyncEngine
import de.bixilon.unithen.ui.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import unithen.common.generated.resources.*

@Composable
private fun Remove(account: Account): (() -> Unit)? {
    var show by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    if (!show) return { show = true }



    if (!deleting) {
        AlertDialog(
            confirmButton = {
                Button({ deleting = true }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Icon(Icons.Filled.Delete, "", tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(Res.string.accounts_option_remove.i18n(), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            },
            dismissButton = { Button({ show = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) { Text("Cancel") } },
            onDismissRequest = { show = false },
            title = { Text(Res.string.accounts_remove_title.i18n()) },
            text = { Text(Res.string.accounts_remove_description.i18n()) },
        )
        return null
    }
    val storage = LocalStorage.current
    val toast = useToast()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // TODO: Revoke token
                storage.accounts.remove(account)
                storage.cleanup()
                toast.invoke(Res.string.accounts_remove_success)
            } finally {
                show = false
                deleting = false
            }
        }
    }


    AlertDialog(
        confirmButton = {},
        onDismissRequest = {},
        title = { Text("Removing...") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Removing account...")
            }
        },
    )


    return null
}

@Composable
private fun AccountOptions(account: Account, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }

    val synchronize = useSyncEngine { syncCourses(account) }
    SyncStatusDialog(synchronize, Res.string.accounts_sync_title.i18n(), Res.string.accounts_sync_description.i18n())

    val remove = Remove(account)

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Sync, "")
                        Text(Res.string.accounts_option_sync.i18n())
                    }
                },
                enabled = !synchronize.active,
                onClick = { expanded = false; synchronize.invoke() }
            )
            DropdownMenuItem(
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Delete, "", tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(Res.string.accounts_option_remove.i18n(), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                },
                onClick = { expanded = false; remove?.invoke() }
            )
        }
    }
}

@Composable
private fun AccountCard(account: Account, onClick: () -> Unit) {
    val site = rememberStorage { sites[account.site]!! }
    val now = useTime()


    val color = when {
        account.authentication.isNullOrBlank() -> MaterialTheme.colorScheme.errorContainer
        now - account.fetched < 4.weeks -> MaterialTheme.colorScheme.primaryContainer // TODO: That color sucks
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
            Column {
                Text(
                    text = account.fullname,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (RuntimeInfo.debug) {
                    Spacer(Modifier.height(8.dp))
                    Text("ID: " + account.uuid.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            AccountOptions(account, Modifier
                .align(Alignment.TopEnd)
                .offset(16.dp, -16.dp) // TODO: why?
            )


            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) {
                val bitmap = remember(site.icon) { site.icon?.toBitmap() }

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
    val accounts = rememberStorageAsync { accounts.all() } ?: return

    Screen {
        ScreenTitle(Res.string.accounts_title.i18n(accounts.size))

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

            FloatingActionButtons {
                FloatingActionButton({ navigator.navigate(AddAccountRoute) }) {
                    Icon(Icons.Filled.Add, "add")
                }
            }
        }
    }
}
