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

package de.bixilon.unithen.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.R


@Composable
@Preview(showBackground = true)
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "logo",
            modifier = Modifier
                .size(300.dp)
        )

        Text(
            text = "UniThen",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This is an unofficial app and NOT affiliated with UniNow GmbH!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Version: ${BuildConfig.VERSION} (${BuildConfig.VERSION_CODE})", textAlign = TextAlign.Center)

                Text(buildAnnotatedString {
                    append("Commit: ")

                    withLink(LinkAnnotation.Url("https://gitlab.bixilon.de/bixilon/unithen/-/commit/${BuildConfig.GIT_COMMIT}")) { append(BuildConfig.GIT_COMMIT) }
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

        Text(
            text = "Licensed under the GNU General Public License v3 or later.\nThis app comes with absolutely NO WARRANTY.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

