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

package de.bixilon.unithen.ui.main.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.ui.main.AddAccountRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation


@Composable
fun SetupScreen() {
    var accepted by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Setup", style = MaterialTheme.typography.headlineLarge)

        Text(buildAnnotatedString {
            append("This app is unofficial and NOT affiliated with UniNow GmbH! Do not report issues to them, instead report them at ")

            withLink(LinkAnnotation.Url("https://gitlab.bixilon.de/bixilon/unithen")) { append("gitlab.bixilon.de/bixilon/unithen") }

            append(".")
        })

        Text("This software was created by Moritz Zwerger and is licensed under the terms of the GPLv3. The app creator is not responsible for any damage to your account. This app comes with absolutely NO warranty.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text("An access token (generated with your credentials) will be saved securely on this device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text("The servers are operated by UniNow GmbH, please check their privacy policy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1.0f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(accepted, { accepted = it })

            Spacer(Modifier.width(8.dp))

            Text("I understand and accept these terms")
        }


        val navigation = LocalNavigation.current
        Button(
            onClick = { navigation.pop(); navigation.navigate(AddAccountRoute) },
            enabled = accepted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add my account")
        }
    }
}
