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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import java.net.URI

@Composable
actual fun WebAuthenticationView(url: URI, callback: (Authentication) -> Unit) {
    val state = rememberTextFieldState()
    Column {
        Text("So, this web view stuff is not implemented on desktop, feel free to paste your session cookie (ory-session) below:")
        TextField(state)

        val disabled = state.text.isBlank() || state.text.length < 30

        Button({ callback.invoke(CookieAuthentication(state.text.toString())) }, enabled = !disabled) {
            Icon(Icons.Default.Add, "")
            Text("Add")
        }
    }
}
