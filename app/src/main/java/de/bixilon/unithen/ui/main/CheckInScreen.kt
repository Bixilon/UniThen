package de.bixilon.unithen.ui.main

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lightspark.composeqr.QrCodeView
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.util.Jackson
import java.util.*


const val CHECK_IN_ROUTE = "/appointment/{id}/checkin"

@Composable
fun CheckInScreen(appointment: Appointment) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        setBrightness(context, 1.0f)

        onDispose {
            setBrightness(context, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        }
    }

    Row {
        Text("Check in")

        QrCodeView(
            data = "https://github.com/lightsparkdev/compose-qr-code",
            modifier = Modifier.size(300.dp)
        )
    }
}

private fun setBrightness(context: Context, level: Float) {
    val window = context.nullCast<Activity>()?.window ?: return

    window.attributes = window.attributes.apply { screenBrightness = level }
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
