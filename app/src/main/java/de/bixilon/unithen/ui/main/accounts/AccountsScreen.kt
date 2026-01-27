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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.main.AccountDetailsRoute
import de.bixilon.unithen.ui.main.AddAccountRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
private fun AccountOptions(account: Account, site: Site, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Update") },
                onClick = {
                    expanded = false
                    // TODO: Progress dialog
                    CoroutineScope(Dispatchers.IO).launch {
                        val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(account.session))
                        val courses = api.postings(account.uuid)

                        DataStorage.STORAGE.populate(site, account, courses)
                    }
                }
            )
            DropdownMenuItem(
                text = { Text("Remove") },
                enabled = false,
                onClick = { expanded = false } // TODO
            )
        }
    }
}

@Composable
private fun AccountCard(account: Account, onClick: () -> Unit) {
    val site = remember { DataStorage.STORAGE.sites[account.site]!! }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
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


            Text(
                text = site.name,
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
fun AccountsScreen() {
    val accounts by remember { DataStorage.STORAGE.accounts.stateOf { all() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "All accounts:",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(Modifier.height(16.dp))

        val navigator = LocalNavigation.current
        LazyColumn(modifier = Modifier.weight(1.0f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items = accounts, key = Account::id) { account -> AccountCard(account) { navigator.navigate(AccountDetailsRoute(account)) } }
        }

        Spacer(Modifier
            .height(16.dp)
            .weight(1.0f))

        Button({ navigator.navigate(AddAccountRoute) }, modifier = Modifier.fillMaxWidth()) { Text("Add account") }
    }
}
