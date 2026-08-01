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

import androidx.compose.runtime.Composable
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.ui.main.AuthenticationCallbackRoute
import de.bixilon.unithen.ui.main.AuthenticationRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage


const val WEB_SESSION_COOKIE_NAME = "ory-session"

@Composable
expect fun LegacyWebviewAuthentication(host: String, callback: (CookieAuthentication) -> Unit)


@Composable
fun LegacyWebviewAuthenticationScreen(host: String) {
    val navigation = LocalNavigation.current
    val storage = LocalStorage.current

    LegacyWebviewAuthentication(host) { navigation.navigate(AuthenticationCallbackRoute(storage.sites[host]!!, it)); navigation.popIf { it is AuthenticationRoute } }
}
