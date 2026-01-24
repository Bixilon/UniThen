package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.main.appointment.AppointmentCard
import java.time.LocalDateTime


const val FAST_CHECK_IN_ROUTE = "/fast_checkin"

@Composable
fun FastCheckInInScreen() {
    val events by remember { derivedStateOf { DataStorage.STORAGE.appointments.getInRange(LocalDateTime.now().minusHours(1), LocalDateTime.now()) } }

    Row {
        Text("Check in")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { item ->
                AppointmentCard(item)
            }
        }
    }
}
