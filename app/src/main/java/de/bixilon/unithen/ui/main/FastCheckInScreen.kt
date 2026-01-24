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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.main.appointment.AppointmentCard
import java.time.Instant
import java.time.ZoneOffset


const val FAST_CHECK_IN_ROUTE = "/fast_checkin"

@Composable
fun FastCheckInInScreen() {
    val now = Instant.ofEpochSecond(1769446901).atZone(ZoneOffset.systemDefault()).toLocalDateTime() // LocalDateTime.now()
    val appointments by remember { derivedStateOf { DataStorage.STORAGE.appointments.getInRange(now.minusHours(1), now) } }

    if (appointments.isEmpty()) {
        Text("No upcoming events!", color = Color.Red, fontSize = 50.sp)
        return
    }

    if (appointments.size == 1) {
        CheckInScreen(appointments.first())
        return
    }

    Row {
        Text("Choose upcoming appointment...")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(appointments) { item ->
                AppointmentCard(item)
            }
        }
    }
}
