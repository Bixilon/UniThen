package de.bixilon.unithen.ui.main.checkin.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.util.i18n
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.scan_no_appointments_details
import unithen.common.generated.resources.scan_no_appointments_message


@Preview
@Composable
fun ScanNoAppointments() {
    SimpleErrorScreen(Res.string.scan_no_appointments_message.i18n(), Res.string.scan_no_appointments_details.i18n())
}
