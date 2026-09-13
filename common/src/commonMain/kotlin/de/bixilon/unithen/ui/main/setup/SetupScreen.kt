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

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.icons.Logo
import de.bixilon.unithen.ui.main.AddAccountRoute
import de.bixilon.unithen.ui.main.SettingsRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.theme.appGradientDark
import de.bixilon.unithen.ui.theme.appGradientLight
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.state.rememberStateOf
import unithen.common.generated.resources.*


@Composable
fun SetupScreen() {
    val navigator = LocalNavigation.current
    var accepted by rememberStateOf { false }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) appGradientDark else appGradientLight),
    ) {
        Screen(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.weight(0.2f))

            Image(
                Logo,
                contentDescription = "logo",
                modifier = Modifier
                    .size(180.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = Res.string.setup_welcome.i18n(),
                modifier = Modifier.padding(horizontal = 30.dp),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp,
                fontSize = 50.sp,
            )

            Spacer(modifier = Modifier.weight(0.4f))

            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = Res.string.setup_disclaimer.i18n(),
                modifier = Modifier.padding(horizontal = 50.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
            )


            Spacer(modifier = Modifier.weight(0.2f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(accepted, { accepted = it })

                Text(
                    text = buildAnnotatedString { append(Res.string.setup_accept.i18n()); withStyle(SpanStyle(Color.Red)) { append(" *") } },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Button(
                onClick = { navigator.navigate(AddAccountRoute) },
                enabled = accepted,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, "")
                Spacer(Modifier.width(4.dp))
                Text(Res.string.setup_login.i18n())
            }

            Spacer(modifier = Modifier.weight(0.2f))

            Text(
                text = Res.string.setup_imprint.i18n(),
                modifier = Modifier.padding(horizontal = 80.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        IconButton(onClick = { navigator.navigate(SettingsRoute) }, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.Settings, "settings")
        }
    }
}
