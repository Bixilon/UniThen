package de.bixilon.unithen.ui.fast

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.bixilon.unithen.ui.error.SimpleErrorScreen


@Preview
@Composable
fun FastCheckinNoAppointments() {
    SimpleErrorScreen("No upcoming courses!", "Are you sure you are there at the right time?")
}
