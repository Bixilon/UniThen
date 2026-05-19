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

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.time.DurationUtil.weeks
import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.api.graphql.http.GraphQlException
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.main.AccountDetailsRoute
import de.bixilon.unithen.ui.main.AddAccountRoute
import de.bixilon.unithen.ui.main.CrashRoute
import de.bixilon.unithen.ui.main.ReauthenticateRoute
import de.bixilon.unithen.ui.main.add.toBitmap
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock


@Composable
private fun AccountOptions(account: Account, site: Site, modifier: Modifier) {
    val context = LocalContext.current
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current
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
            DropdownMenuItem(
                text = { Text(if (refreshing) "Updating..." else "Update") },
                enabled = !refreshing,
                onClick = {
                    refreshing = true
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(account.session))
                            val courses = api.courses(account.uuid) ?: return@launch

                            storage.populate(site, account, courses)
                            withContext(Dispatchers.Main) { Toast.makeText(context, "Account refreshed!", Toast.LENGTH_SHORT).show() }
                        } catch (_: AuthenticationException) {
                            storage.accounts.logout(account)
                            navigation.navigate(ReauthenticateRoute(site))
                        } catch (error: GraphQlException) {
                            navigation.navigate(CrashRoute(error))
                        } catch (error: Throwable) {
                            navigation.navigate(CrashRoute(error))
                        } finally {
                            withContext(Dispatchers.Main) { refreshing = false }
                        }
                    }

                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Remove") },
                enabled = false,
                onClick = { expanded = false } // TODO
            )
        }
    }


    if (refreshing) {
        AlertDialog(
            confirmButton = {},
            onDismissRequest = {},
            title = { Text("Refreshing account...") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
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
    val storage = LocalStorage.current
    val site = remember { storage.sites[account.site]!! }
    remember(site.icon) { site.icon?.toBitmap()?.asImageBitmap() }


    val color = when {
        account.session.isBlank() -> MaterialTheme.colorScheme.errorContainer
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

            AccountOptions(account, site, Modifier
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
    val storage = LocalStorage.current
    val accounts by remember { storage.accounts.stateOf { all() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            "Accounts (${accounts.size}):",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(Modifier.height(16.dp))

        val navigator = LocalNavigation.current
        LazyColumn(modifier = Modifier.weight(1.0f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items = accounts, key = Account::id) { account -> AccountCard(account) { navigator.navigate(AccountDetailsRoute(account)) } }
        }

        Spacer(Modifier.height(8.dp))

        Button({ navigator.navigate(AddAccountRoute) }, modifier = Modifier.fillMaxWidth()) { Text("Add account") }
    }
}
