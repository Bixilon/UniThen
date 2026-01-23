package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.util.Jackson
import java.util.*


const val CHECK_IN_ROUTE = "/appointment/{id}/checkin"

@Composable
fun AppointmentsScreen(id: Key) {

    Row {
        Text("Appointments")

    }
}

fun createQrCode(user: UUID, appointment: UUID, firstname: String, lastname: String): String {
    val node = Jackson.MAPPER.createObjectNode().apply {
        put("appointment_id", appointment.toString())
        put("user_id", user.toString())
        replace("userName", Jackson.MAPPER.createObjectNode().apply {
            put("last", lastname)
            put("first", firstname)
        })
    }

    return node.toString()
}
