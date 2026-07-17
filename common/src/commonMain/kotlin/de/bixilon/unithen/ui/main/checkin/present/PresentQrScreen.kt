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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.string.StringUtil.truncate
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.InfoPair
import de.bixilon.unithen.ui.navigation.LocalVisibility
import de.bixilon.unithen.ui.util.QrCode
import de.bixilon.unithen.ui.util.ScreenBrightnessOverride
import de.bixilon.unithen.ui.util.TimeFormatUtil.format
import de.bixilon.unithen.ui.util.i18n
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import unithen.common.generated.resources.*
import kotlin.uuid.Uuid


@Composable
fun PresentQrScreen(account: Account, course: Course, appointment: Appointment) {
    val visible = LocalVisibility.current
    val name by rememberSetting(Settings.QR_CODE_REMOVE_NAME)

    if (visible) {
        ScreenBrightnessOverride(1.0f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = course.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        Box(Modifier.padding(4.dp)) {
            val (firstname, lastname) = if (name) Pair("A", "B") else Pair(account.firstname, account.lastname)

            QrCode(
                data = createQrCode(account.uuid, appointment.uuid, firstname.truncate(12), lastname.truncate(12)),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f, matchHeightConstraintsFirst = true) // TODO: broken in scroll container
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
                    .padding(6.dp)
            )
        }

        Text(
            text = Res.string.present_show_entrance.i18n(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(24.dp))


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
    }
}


fun createQrCode(user: Uuid, appointment: Uuid, firstname: String, lastname: String): String {
    val node = JsonObject(mapOf(
        "appointment_id" to JsonPrimitive(appointment.toString()),
        "user_id" to JsonPrimitive(user.toString()),
        "userName" to JsonObject(mapOf(
            "last" to JsonPrimitive(lastname),
            "first" to JsonPrimitive(firstname),
        )),
    ))

    return node.toString()
}
