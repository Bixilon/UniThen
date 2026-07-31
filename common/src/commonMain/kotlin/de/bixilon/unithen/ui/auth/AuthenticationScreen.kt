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

package de.bixilon.unithen.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.main.MainScreens
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.sync.SyncEngineCompleteEffect
import de.bixilon.unithen.ui.sync.status.SyncStatusDialog
import de.bixilon.unithen.ui.sync.useSyncEngine
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.state.rememberStateOf
import de.bixilon.unithen.ui.util.useAsyncNetwork
import unithen.common.generated.resources.*

@Composable
fun FetchUserDetails(site: Site, authentication: Authentication, callback: (Account) -> Unit) {
    val storage = LocalStorage.current

    val fetch = useAsyncNetwork {
        val api = AuthenticatedUniNowApi(site.host, authentication)
        val details = api.getUserDetails()

        val account = storage.transaction { it.accounts.add(site, details, authentication) }
        callback.invoke(account)
    }

    LaunchedEffect(Unit) { fetch.invoke() }

    AlertDialog(
        confirmButton = {},
        onDismissRequest = {},
        title = { Text(Res.string.authentication_loading.i18n()) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(Res.string.authentication_fetching_user_details.i18n())
                Text(Res.string.authentication_take_a_while.i18n(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}


@Composable
fun AuthenticationScreen(site: Site, callback: (Account) -> Unit) {
    val storage = LocalStorage.current
    var authentication: Authentication? by rememberStateOf { null }
    var account: Account? by rememberStateOf { null }
    var entrypoint by rememberSetting(Settings.ENTRYPOINT, MainScreens)

    if (authentication == null) {
        WebAuthenticationView(host = site.host) { authentication = it }
        return
    }

    val first = remember(Unit) { storage.accounts.count == 0 }

    if (account == null) {
        FetchUserDetails(site, authentication!!) { account = it }
        return
    }

    val synchronize = useSyncEngine { syncCourses(account!!) }
    LaunchedEffect(Unit) { synchronize.invoke(force = true) }

    SyncEngineCompleteEffect(synchronize) {
        when {
            !first -> Unit
            storage.courses.isTutor() -> entrypoint = MainScreens.CHECKIN_SCAN
            storage.courses.isEnrolled() -> entrypoint = MainScreens.CHECKIN_PRESENT
        }
    }

    SyncEngineCompleteEffect(synchronize) { callback.invoke(account!!) }


    SyncStatusDialog(synchronize, Res.string.authentication_loading.i18n(), Res.string.authentication_fetching.i18n())
}
