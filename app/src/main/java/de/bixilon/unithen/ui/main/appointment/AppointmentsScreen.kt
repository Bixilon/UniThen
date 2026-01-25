package de.bixilon.unithen.ui.main.appointment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.navigation.Navigator


@Composable
fun AppointmentCard(appointment: Appointment) {
    Card {
        Text(appointment.uuid.toString())
        Text(appointment.start.toString())
    }
}

@Composable
fun AppointmentsScreen(navigator: Navigator) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(DataStorage.STORAGE.appointments.all()) { item ->
            AppointmentCard(item)
        }
    }
}
