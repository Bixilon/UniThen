package de.bixilon.unithen.ui.main.checkin.scan.qr.confirm

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.sync.useSyncEngine
import de.bixilon.unithen.ui.util.TimeFormatUtil.formatNow
import de.bixilon.unithen.ui.util.i18n
import unithen.common.generated.resources.*

@Composable
private fun EnrolledListWarning(course: Course) {
    if (!course.isEnrolledStale()) return

    val synchronize = useSyncEngine { syncEnrolled(course) }

    LaunchedEffect(Unit) { synchronize.invoke() }

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        if (synchronize.active) {
            CircularProgressIndicator(modifier = Modifier.padding(horizontal = 16.dp)); Text(Res.string.scan_updating_enrolled.i18n())
        } else {
            Icon(Icons.Default.Warning, "", tint = Color.Yellow); Spacer(Modifier.width(16.dp)); Text(Res.string.scan_enrolled_outdated.i18n(course.fetched.formatNow()))
        }
    }
}

@Composable
fun ColumnScope.ScanQrNotEnrolled(user: User?, course: Course, appointment: Appointment) {
    val message = when {
        user == null -> Res.string.scan_error_unknown_user.i18n()
        else -> Res.string.scan_error_not_enrolled.i18n()
    }

    ConfirmScreenWarning(Icons.Filled.Close, Color.Red, message)

    EnrolledListWarning(course)
    Spacer(Modifier.height(16.dp))

    DetailsContainer(user, appointment)

    Spacer(Modifier
        .weight(1.0f)
        .defaultMinSize(minHeight = 16.dp))

    ConfirmScreenActions(user, confirm = null)
}
