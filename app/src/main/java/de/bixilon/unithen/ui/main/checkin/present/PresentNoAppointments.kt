package de.bixilon.unithen.ui.main.checkin.present

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.bixilon.unithen.R
import de.bixilon.unithen.ui.error.SimpleErrorScreen
import de.bixilon.unithen.ui.util.i18n


@Preview
@Composable
fun PresentNoAppointments() {
    SimpleErrorScreen(R.string.present_no_appointments_message.i18n(), R.string.present_no_appointments_details.i18n())
}
