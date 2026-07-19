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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle

@Composable
fun SessionCookieAuthentication(host: String, callback: (Authentication) -> Unit) {
    val state = rememberTextFieldState()

    Screen {
        ScreenTitle("Cookie authentication")
        Text("Please paste your session cookie from $host (ory-session) below:")
        Text("If you don't know how this works, you must visit the website, login and then press [F12], check in the network tab and find the \"Cookie\" header and extract it.")
        TextField(state, modifier = Modifier.fillMaxWidth(), lineLimits = TextFieldLineLimits.SingleLine)

        val disabled = state.text.isBlank() || state.text.length < 30

        Button({ callback.invoke(CookieAuthentication(state.text.toString())) }, enabled = !disabled, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, "")
            Text("Add")
        }
    }
}
