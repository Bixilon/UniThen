package de.bixilon.unithen.ui.main.checkin.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.bixilon.unithen.R
import de.bixilon.unithen.ui.error.SimpleErrorScreen


@Preview
@Composable
fun ScanNoAppointments() {
    SimpleErrorScreen(stringResource(R.string.scan_no_appointments_message), stringResource(R.string.scan_no_appointments_details))
}
