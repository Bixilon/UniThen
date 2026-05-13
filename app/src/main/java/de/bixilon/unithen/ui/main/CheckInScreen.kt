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

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lightspark.composeqr.QrCodeView
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.rememberSetting
import de.bixilon.unithen.ui.util.UiUtil.format
import de.bixilon.unithen.util.json.Jackson
import java.util.*


@Composable
fun CheckInScreen(account: Account, course: Course, appointment: Appointment) {
    var fakeName by rememberSetting(Settings.QR_CODE_FAKE_NAME)

    val context = LocalContext.current
    DisposableEffect(Unit) {
        setBrightness(context, 1.0f)

        onDispose {
            setBrightness(context, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))


            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // TODO: color date differently if passed/upcoming
                    Text(
                        text = "Start: ${appointment.start.format()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "End: ${appointment.end.format()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Location: ${appointment.location}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "Name: ${account.firstname} ${account.lastname}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Box(Modifier.padding(16.dp)) {
                val (firstname, lastname) = if (fakeName) Pair("Max", "Muster") else Pair(account.firstname, account.lastname)
                QrCodeView(
                    data = createQrCode(account.uuid, appointment.uuid, firstname, lastname),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f, matchHeightConstraintsFirst = true)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White)
                        .padding(16.dp)
                )
            }

            Text(
                text = "Present this QR code at the entrance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun setBrightness(context: Context, level: Float) {
    val window = context.nullCast<Activity>()?.window ?: return

    window.attributes = window.attributes.apply { screenBrightness = level }
}

fun createQrCode(user: UUID, appointment: UUID, firstname: String, lastname: String): String {
    val node = Jackson.MAPPER.createObjectNode().apply {
        put("appointment_id", appointment.toString())
        put("user_id", user.toString())
        replace("userName", Jackson.MAPPER.createObjectNode().apply {
            put("last", lastname)
            put("first", firstname)
        })
    }

    return node.toString()
}
