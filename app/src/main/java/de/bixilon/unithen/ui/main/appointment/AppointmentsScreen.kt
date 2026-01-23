package de.bixilon.unithen.ui.main.appointment

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController


const val APPOINTMENTS_ROUTE = "/appointmenets"

@Composable
fun AppointmentsScreen(navigation: NavController) {

    Row {
        Text("Appointments")
    }
}
