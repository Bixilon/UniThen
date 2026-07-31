package de.bixilon.unithen.ui.main.checkin.scan.qr.confirm

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.theme.checkInSuccess
import de.bixilon.unithen.ui.util.i18n
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.back
import unithen.common.generated.resources.scan_confirm_ok
import unithen.common.generated.resources.scan_confirm_ok_network


@Composable
fun ColumnScope.ScanQrConfirmed(user: User, appointment: Appointment) {
    val queue = rememberStorage { checkInQueue[appointment, user] }
    val navigation = LocalNavigation.current
    ConfirmScreenWarning(Icons.Filled.CheckCircle, checkInSuccess, null)

    Text(Res.string.scan_confirm_ok.i18n(), textAlign = TextAlign.Center)

    if (queue != null) {
        Text(Res.string.scan_confirm_ok_network.i18n(), color = Color.Red, textAlign = TextAlign.Center)
    }

    Spacer(Modifier.height(16.dp))

    DetailsContainer(user, appointment)

    Spacer(Modifier
        .weight(1.0f)
        .defaultMinSize(minHeight = 16.dp))


    Button({ navigation.pop() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
        Icon(Icons.AutoMirrored.Filled.ArrowLeft, "back")
        Text(Res.string.back.i18n())
    }
}
