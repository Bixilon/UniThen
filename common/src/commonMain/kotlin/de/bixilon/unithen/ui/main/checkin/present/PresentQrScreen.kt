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

package de.bixilon.unithen.ui.main.checkin.present

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.settings.FeatureFlags
import de.bixilon.unithen.settings.QrVersion
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.components.qr.QrCode
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.InfoPair
import de.bixilon.unithen.ui.main.checkin.present.QrEncoder.encodeQr
import de.bixilon.unithen.ui.navigation.LocalVisibility
import de.bixilon.unithen.ui.util.ScreenBrightnessOverride
import de.bixilon.unithen.ui.util.TimeFormatUtil.format
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.state.rememberStateOf
import unithen.common.generated.resources.*


@Composable
fun PresentQrScreen(account: Account, course: Course, appointment: Appointment) {
    val visible = LocalVisibility.current

    if (visible) {
        ScreenBrightnessOverride(1.0f)
    }
    var active by rememberStateOf { false }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    active = true

                    waitForUpOrCancellation()
                    active = false
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = course.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        InfoContainer(modifier = Modifier
            .padding(horizontal = 16.dp)) {
            InfoPair(Res.string.course_name.i18n(), account.fullname)
            InfoPair(Res.string.appointment_start.i18n(), appointment.start.format())
            InfoPair(Res.string.appointment_end.i18n(), appointment.end.format())
            InfoPair(Res.string.appointment_location.i18n(), appointment.location)
            if (RuntimeInfo.debug) {
                InfoPair("ID", appointment.uuid.toString())
            }
        }

        Spacer(modifier = Modifier.height(8.dp))


        BoxWithConstraints(Modifier.weight(1.0f).padding(4.dp).widthIn(min = 100.dp).heightIn(min = 100.dp)) {
            val qr = minOf(maxWidth, maxHeight - 4.dp - 45.dp)
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

                val version by rememberSetting(FeatureFlags.QR_VERSION)
                val encoded = remember(account, appointment, active) { encodeQr(if (active) QrVersion.V1 else version, account.uuid, appointment.uuid, account.firstname, account.lastname) }

                QrCode(data = encoded, modifier = Modifier.size(qr))
                if (!active && version != QrVersion.V1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This QR code is experimental: $version",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Res.string.present_show_entrance.i18n(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
