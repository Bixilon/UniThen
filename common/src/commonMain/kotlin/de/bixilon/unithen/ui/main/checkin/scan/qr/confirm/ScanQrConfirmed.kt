package de.bixilon.unithen.ui.main.checkin.scan.qr.confirm

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.theme.checkInSuccess
import de.bixilon.unithen.ui.util.i18n
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.scan_confirm_ok


@Composable
fun ColumnScope.ScanQrConfirmed(user: User?, appointment: Appointment) {
    ConfirmScreenWarning(Icons.Filled.CheckCircle, checkInSuccess, Res.string.scan_confirm_ok.i18n())

    Spacer(Modifier.height(16.dp))

    DetailsContainer(user, appointment)

    Spacer(Modifier
        .weight(1.0f)
        .defaultMinSize(minHeight = 16.dp))

    ConfirmScreenActions(user, confirm = null)
}
