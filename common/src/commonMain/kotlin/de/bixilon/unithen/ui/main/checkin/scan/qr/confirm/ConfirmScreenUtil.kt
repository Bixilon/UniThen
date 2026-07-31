package de.bixilon.unithen.ui.main.checkin.scan.qr.confirm

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.InfoPair
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.main.checkin.scan.Contributors.isMajorContributor
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.TimeFormatUtil.format
import de.bixilon.unithen.ui.util.i18n
import unithen.common.generated.resources.*

val ICON_SIZE = Modifier
    .height(200.dp)
    .width(200.dp)

@Composable
fun ConfirmScreenWarning(icon: ImageVector, color: Color, message: String?) {
    Icon(icon, "", tint = color, modifier = ICON_SIZE)

    message?.let { ErrorBox(it) }
}

@Composable
fun ConfirmScreenActions(user: User?, cancel: Boolean = true, confirm: (() -> Unit)? = null) {
    val navigation = LocalNavigation.current

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Button({ navigation.pop() }, modifier = Modifier.fillMaxWidth(), enabled = cancel, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
            Icon(Icons.Filled.Close, "cancel")
            Text(Res.string.cancel.i18n())
        }

        Button({ confirm?.invoke() }, enabled = confirm != null, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Check, "check")
            Text((if (user != null && user.isMajorContributor()) Res.string.scan_confirm_contributor else Res.string.scan_confirm).i18n())
        }
    }
}

@Composable
fun DetailsContainer(user: User?, appointment: Appointment) {
    InfoContainer(modifier = Modifier.fillMaxWidth(0.8f)) {
        user?.let { InfoPair(Res.string.user_name.i18n(), "${user.firstname} ${user.lastname}") }
        InfoPair(Res.string.appointment_start.i18n(), appointment.start.format())
        InfoPair(Res.string.appointment_end.i18n(), appointment.end.format())
        InfoPair(Res.string.appointment_location.i18n(), appointment.location)
    }
}

