package de.bixilon.unithen.ui.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lightspark.composeqr.QrCodeView
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.util.Jackson
import java.util.*


const val CHECK_IN_ROUTE = "/appointment/{id}/checkin"

@Composable
fun CheckInScreen(id: Key) {

    Row {
        Text("Check in")

        QrCodeView(
            data = "https://github.com/lightsparkdev/compose-qr-code",
            modifier = Modifier.size(300.dp)
        )
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
