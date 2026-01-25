package de.bixilon.unithen.ui.main

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lightspark.composeqr.QrCodeView
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.util.Jackson
import java.util.*



@Composable
fun CheckInScreen(account: Account, course: Course, appointment: Appointment) {
    var fakeName by remember { mutableStateOf(false) }
    var xss by remember { mutableStateOf(false) }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        setBrightness(context, 1.0f)

        onDispose {
            setBrightness(context, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Start: ${appointment.start}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "End: ${appointment.end}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Name: ${account.firstname} ${account.lastname}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            QrCodeView(
                data = createQrCode(account.uuid, appointment.uuid, if (xss) "Hello<script>alert(1)</script>\"'Hm." else if (fakeName) "Max" else account.firstname, if (fakeName) "Muster" else account.lastname),
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Present this QR code at the entrance",
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = fakeName,
                    onCheckedChange = { fakeName = it }
                )
                Text("Fake name (Max Muster)")
            }

            // TODO: remove
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = xss,
                    onCheckedChange = { xss = it }
                )
                Text("Try XSS")
            }
        }
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
