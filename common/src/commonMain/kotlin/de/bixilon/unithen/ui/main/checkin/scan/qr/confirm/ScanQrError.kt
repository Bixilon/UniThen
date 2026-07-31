package de.bixilon.unithen.ui.main.checkin.scan.qr.confirm

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.CheckInQueue
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.util.i18n
import unithen.common.generated.resources.*

@Composable
fun ColumnScope.ScanQrError(user: User, attendee: Boolean, queue: CheckInQueue?, appointment: Appointment) {
    val message = when {
        queue?.attempt != null -> Res.string.scan_error_check_out_pending.i18n()
        queue?.message != null -> Res.string.scan_unknown_error_server.i18n(queue.message)
        queue != null -> Res.string.scan_error_check_in_pending.i18n()
        attendee -> Res.string.scan_error_already_checked_in.i18n()
        else -> "Unknown error"
    }

    if (queue?.message != null) {
        ConfirmScreenWarning(Icons.Filled.Close, Color.Red, message)
    } else {
        ConfirmScreenWarning(Icons.Filled.Warning, Color.Yellow, message)
    }

    Spacer(Modifier.height(16.dp))

    DetailsContainer(user, appointment)

    Spacer(Modifier
        .weight(1.0f)
        .defaultMinSize(minHeight = 16.dp))

    ConfirmScreenActions(user, confirm = null)
}

@Composable
fun ColumnScope.ScanQrError(user: User, message: String, appointment: Appointment) {
    ConfirmScreenWarning(Icons.Filled.Close, Color.Red, message)

    Spacer(Modifier.height(16.dp))

    DetailsContainer(user, appointment)

    Spacer(Modifier
        .weight(1.0f)
        .defaultMinSize(minHeight = 16.dp))

    ConfirmScreenActions(user, confirm = null)
}
