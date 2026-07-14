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
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchFromCourses
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.main.MainScreens
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useAsyncNetwork
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import unithen.common.generated.resources.*


@Composable
fun Fetch(site: Site, authentication: Authentication, callback: () -> Unit) {
    val storage = LocalStorage.current
    var entrypoint by rememberSetting(Settings.ENTRYPOINT, MainScreens)

    var message by remember { mutableStateOf(runBlocking { getString(Res.string.authentication_fetching_user_details) }) }


    val fetch = useAsyncNetwork<Unit>(null) {
        val first = storage.accounts.count == 0
        val api = AuthenticatedUniNowApi(site.url, authentication)
        val details = api.getUserDetails()

        val account = storage.transaction { it.accounts.add(site, details, authentication) }

        message = getString(Res.string.authentication_course_list)

        storage.fetchFromCourses(account, true) { message = runBlocking { getString(Res.string.authentication_fetching, it.course, it.courses) } }

        when {
            !first -> Unit
            storage.courses.isTutor() -> entrypoint = MainScreens.CHECKIN_SCAN
            storage.courses.isNotTutor() -> entrypoint = MainScreens.CHECKIN_PRESENT
        }

        callback.invoke()
    }

    LaunchedEffect(Unit) { fetch.invoke(Unit) }

    AlertDialog(
        confirmButton = {},
        onDismissRequest = {},
        title = { Text(Res.string.authentication_loading.i18n()) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
                Text(Res.string.authentication_take_a_while.i18n(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

@Composable
fun AuthenticationScreen(site: Site, callback: (Authentication) -> Unit) {
    var authentication: Authentication? by remember { mutableStateOf(null) }

    if (authentication == null) {
        WebAuthenticationView(url = site.url) { authentication = it }
        return
    }

    Fetch(site, authentication!!) { callback.invoke(authentication!!) }
}
