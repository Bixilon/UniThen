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

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.exception.Broken
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetch
import de.bixilon.unithen.api.user.UserDetails
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.ui.error.CrashScreen
import de.bixilon.unithen.ui.storage.LocalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


enum class AuthenticationState {
    FETCH_USER_DETAILS,
    FETCH_COURSES,
    DONE,
}

@Composable
fun AuthenticationProgress(state: AuthenticationState) {
    val text = when (state) {
        AuthenticationState.FETCH_USER_DETAILS -> "Fetching user details..."
        AuthenticationState.FETCH_COURSES -> "Fetching courses..."
        AuthenticationState.DONE -> Broken()
    }
    AlertDialog(
        confirmButton = {},
        onDismissRequest = {},
        title = { Text("Logging in...") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text)
            }
        },
    )
}

private fun fetchUserDetails(storage: SqlStorage, site: Site, authentication: Authentication, callback: (state: AuthenticationState) -> Unit) {
    Log.i("Auth", "Fetching user details...")
    val details = UserDetails.fetch(site.url, authentication)

    Log.v("Auth", "Found user details: $details")

    val account = storage.transaction { it.accounts.add(site, details, authentication) }

    callback.invoke(AuthenticationState.FETCH_COURSES)

    Log.i("Auth", "Fetching courses...")

    storage.fetch(account)

    Log.i("Auth", "Courses fetched")
    callback.invoke(AuthenticationState.DONE)
}

@Composable
fun AuthenticationScreen(site: Site, callback: (Authentication) -> Unit) {
    val storage = LocalStorage.current
    var authentication: Authentication? by remember { mutableStateOf(null) }
    var state by remember { mutableStateOf(AuthenticationState.FETCH_USER_DETAILS) }
    var error: Throwable? by remember { mutableStateOf(null) }

    LaunchedEffect(authentication) {
        val authentication = authentication ?: return@LaunchedEffect

        withContext(Dispatchers.IO) {
            try {
                fetchUserDetails(storage, site, authentication) { state = it }
                callback.invoke(authentication)
            } catch (_error: Throwable) {
                Log.e("Auth", "Error fetching user details: $_error")
                _error.printStackTrace()
                error = _error
            }
        }
    }


    if (error != null) {
        CrashScreen("Error fetching user details", error!!)
        return
    }

    if (authentication == null) {
        WebAuthenticationView(url = site.url) { authentication = it }
        return
    }
    if (state == AuthenticationState.DONE) {
        Text("Done")
        return
    }

    AuthenticationProgress(state)
}
