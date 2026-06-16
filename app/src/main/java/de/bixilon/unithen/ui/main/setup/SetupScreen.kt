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
import de.bixilon.unithen.R
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.AddAccountRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.i18n


@Composable
private fun Note(text: String) = Text(text,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
fun SetupScreen() {
    var accepted by rememberSaveable { mutableStateOf(false) }

    Screen {
        ScreenTitle(R.string.setup_title.i18n())

        Column(modifier = Modifier.weight(1.0f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(buildAnnotatedString {
                append(R.string.setup_1.i18n())
                append(' ')

                withLink(LinkAnnotation.Url("https://gitlab.bixilon.de/bixilon/unithen")) { append("gitlab.bixilon.de/bixilon/unithen") }

                append(".")
            })

            Note(R.string.setup_2.i18n())

            Note(R.string.setup_3.i18n())

            Note(R.string.setup_4.i18n())

            Spacer(modifier = Modifier.weight(1.0f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(accepted, { accepted = it })

                Spacer(Modifier.width(4.dp))

                Text(R.string.set_accept.i18n())
            }
        }


        val navigation = LocalNavigation.current
        Button(
            onClick = { navigation.navigate(AddAccountRoute) },
            enabled = accepted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(R.string.setup_login.i18n())
        }
    }
}
