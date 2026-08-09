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

package de.bixilon.unithen.ui.main.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildInfo
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.icons.Logo
import de.bixilon.unithen.ui.main.FeatureFlagRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.rememberIsFdroid
import de.bixilon.unithen.ui.util.verticalScrollWithBar
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.about_license
import unithen.common.generated.resources.about_unofficial
import unithen.common.generated.resources.app_name

@Composable
fun AboutScreen() {
    val navigator = LocalNavigation.current
    Screen(modifier = Modifier.verticalScrollWithBar(), horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            Logo,
            contentDescription = "logo",
            modifier = Modifier
                .clip(RoundedCornerShape(35.dp))
                .combinedClickable(onClick = {}, onLongClick = { navigator.navigate(FeatureFlagRoute) })
                .size(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Res.string.app_name.i18n(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = Res.string.about_unofficial.i18n(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Version: ${BuildInfo.VERSION} (${BuildInfo.VERSION_CODE})", textAlign = TextAlign.Center)

                if (RuntimeInfo.debug) {
                    Text("This is a DEBUG build!", color = Color.Red)
                }


                if (rememberIsFdroid()) {
                    Text("Installed from F-Droid\uD83C\uDF89", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Text(buildAnnotatedString {
                    append("Commit: ")

                    withLink(LinkAnnotation.Url("https://gitlab.bixilon.de/bixilon/unithen/-/commit/${BuildInfo.GIT_COMMIT}")) { append(BuildInfo.GIT_COMMIT) }
                }, textAlign = TextAlign.Center)

                Text(buildAnnotatedString {
                    append("Issues: ")

                    withLink(LinkAnnotation.Url("https://gitlab.bixilon.de/bixilon/unithen/-/issues")) { append("gitlab.bixilon.de/bixilon/unithen/-/issues") }
                }, textAlign = TextAlign.Center)

                Text(buildAnnotatedString {
                    append("Source Code: ")

                    withLink(LinkAnnotation.Url("https://gitlab.bixilon.de/bixilon/unithen")) { append("gitlab.bixilon.de/bixilon/unithen") }
                }, textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!RuntimeInfo.debug) {
            UpdateCheckButton()
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = Res.string.about_license.i18n(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

