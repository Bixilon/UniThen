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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.time.DurationUtil.weeks
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.R
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchFromAppointments
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchFromCourses
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.AccountDetailsRoute
import de.bixilon.unithen.ui.main.AddAccountRoute
import de.bixilon.unithen.ui.main.add.toBitmap
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.rememberSetting
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.storage.rememberStorageAsync
import de.bixilon.unithen.ui.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
private fun Sync(account: Account): (() -> Unit)? {
    val storage = LocalStorage.current
    val fetchAppointments by rememberSetting(Settings.FETCH_APPOINTMENTS)
    val synchronize = useAsyncNetwork<Unit>(account) { if (fetchAppointments) storage.fetchFromAppointments(account, true) else storage.fetchFromCourses(account, true) }

    var running by remember { mutableStateOf(false) } // TODO: Abort actually


    if (running || !synchronize.active) return {
        try {
            running = true
            synchronize.invoke(Unit)
        } finally {
            running = false
        }
    }




    AlertDialog(
        confirmButton = {},
        onDismissRequest = { running = false },
        title = { Text(R.string.accounts_sync_title.i18n()) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(R.string.accounts_sync_description.i18n())
            }
        },
    )

    return null
}

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
                    Text(R.string.accounts_option_remove.i18n(), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            },
            dismissButton = { Button({ show = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) { Text("Cancel") } },
            onDismissRequest = { show = false },
            title = { Text(R.string.accounts_remove_title.i18n()) },
            text = { Text(R.string.accounts_remove_description.i18n()) },
        )
        return null
    }
    val storage = LocalStorage.current
    val toast = useToast()

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // TODO: Revoke token
                storage.accounts.remove(account)
                storage.cleanup()
                toast.invoke(R.string.accounts_remove_success)
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

    val sync = Sync(account)
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
                        Text(R.string.accounts_option_sync.i18n())
                    }
                },
                onClick = { expanded = false; sync?.invoke() }
            )
            DropdownMenuItem(
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Delete, "", tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(R.string.accounts_option_remove.i18n(), color = MaterialTheme.colorScheme.onErrorContainer)
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
        account.sessionKey.isNullOrBlank() -> MaterialTheme.colorScheme.errorContainer
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
                if (BuildConfig.DEBUG) {
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
    val accounts = rememberStorageAsync { accounts.all() } ?: return

    Screen {
        ScreenTitle(R.string.accounts_title.i18n(accounts.size))

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
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp)
            ) {
                Icon(Icons.Filled.Add, "add")
            }
        }
    }
}
