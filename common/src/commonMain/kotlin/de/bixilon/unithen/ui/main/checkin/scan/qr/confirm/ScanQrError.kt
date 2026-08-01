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
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.qr.QrScanResult
import de.bixilon.unithen.ui.util.i18n
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.scan_unknown_error_server

@Composable
fun ColumnScope.ScanQrError(user: User, result: QrScanResult.SoftError) {
    if (result is QrScanResult.Rejected) {
        ConfirmScreenWarning(Icons.Filled.Close, Color.Red, Res.string.scan_unknown_error_server.i18n(result.message))
    } else {
        ConfirmScreenWarning(Icons.Filled.Warning, Color.Yellow, result.label.i18n())
    }

    Spacer(Modifier.height(16.dp))

    DetailsContainer(user, result.appointment)

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
