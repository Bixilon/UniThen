package de.bixilon.unithen.ui.main.checkin.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.bixilon.unithen.R
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.util.i18n


@Preview
@Composable
fun ScanNoAppointments() {
    SimpleErrorScreen(R.string.scan_no_appointments_message.i18n(), R.string.scan_no_appointments_details.i18n())
}
