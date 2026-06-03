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

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lightspark.composeqr.QrCodeView
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.string.StringUtil.truncate
import de.bixilon.unithen.R
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.InfoPair
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.rememberSetting
import de.bixilon.unithen.ui.navigation.LocalVisibility
import de.bixilon.unithen.ui.util.UiUtil.format
import de.bixilon.unithen.util.json.Jackson
import java.util.*


@Composable
fun CheckInQrPresentScreen(account: Account, course: Course, appointment: Appointment) {
    val visible = LocalVisibility.current
    val name by rememberSetting(Settings.QR_CODE_REMOVE_NAME)

    val context = LocalContext.current
    DisposableEffect(visible) {
        if (visible) {
            setBrightness(context, 1.0f)
        }

        onDispose {
            setBrightness(context, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = course.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))


        InfoContainer(modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(0.8f)) {
            InfoPair(stringResource(R.string.course_name), "${account.firstname} ${account.lastname}")
            InfoPair(stringResource(R.string.appointment_start), appointment.start.format())
            InfoPair(stringResource(R.string.appointment_end), appointment.end.format())
            InfoPair(stringResource(R.string.appointment_location), appointment.location)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(Modifier.padding(4.dp)) {
            val (firstname, lastname) = if (name) Pair("A", "B") else Pair(account.firstname, account.lastname)

            QrCodeView(
                data = createQrCode(account.uuid, appointment.uuid, firstname.truncate(12), lastname.truncate(12)),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
                    .padding(6.dp)
            )
        }

        Text(
            text = stringResource(R.string.present_show_entrance),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
