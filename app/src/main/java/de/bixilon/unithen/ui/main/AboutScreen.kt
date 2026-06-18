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

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.R
import de.bixilon.unithen.api.HttpUtil
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.icons.Logo
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.rememberIsFdroid
import de.bixilon.unithen.ui.util.useAsyncNetwork
import okhttp3.OkHttpClient


@Composable
fun UpdateChecker() {
    val context = LocalContext.current
    var next by remember { mutableIntStateOf(-1) }

    LaunchedEffect(next) {
        if (next > 0 && next > BuildConfig.VERSION_CODE) {
            context.startActivity(Intent(Intent.ACTION_VIEW, "https://gitlab.bixilon.de/bixilon/unithen/-/releases".toUri()))
        }
    }

    val check = useAsyncNetwork<Unit>(null) {
        val request = HttpUtil.create("https://gitlab.bixilon.de".toURI(), "/bixilon/unithen/-/raw/master/fdroid.txt")
            .get()
            .build()

        val client = OkHttpClient().newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        val response = client.newCall(request).execute()

        if (response.code != 200) throw IllegalStateException("Request is not OK")

        // Same regex as for fdroid: https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/de.bixilon.unithen.yml
        next = Regex("^(\\d+)$", RegexOption.MULTILINE).find(response.body.string())!!.groups[1]!!.value.toInt()
    }


    Button({ check.invoke(Unit) }, enabled = !check.active && next < 0) {
        Icon(Icons.Default.Update, "")
        Spacer(Modifier.width(8.dp))
        Text(when {
            check.active -> "Checking for updates..."
            next > 0 && next <= BuildConfig.VERSION_CODE -> "No update available!"
            next > BuildConfig.VERSION_CODE -> "Update available!"
            else -> "Check for updates"
        })
    }
}

@Composable
@Preview(showBackground = true)
fun AboutScreen() {
    Screen(modifier = Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            Logo,
            contentDescription = "logo",
            modifier = Modifier
                .size(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = R.string.app_name.i18n(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = R.string.about_unofficial.i18n(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Version: ${BuildConfig.VERSION} (${BuildConfig.VERSION_CODE})", textAlign = TextAlign.Center)

                if (BuildConfig.DEBUG) {
                    Text("This is a DEBUG build!", color = Color.Red)
                }


                if (rememberIsFdroid()) {
                    Text("Installed from F-Droid\uD83C\uDF89", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

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

        if (!BuildConfig.DEBUG) {
            UpdateChecker()
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = R.string.about_license.i18n(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

