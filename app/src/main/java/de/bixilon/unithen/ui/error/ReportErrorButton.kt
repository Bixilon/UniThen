package de.bixilon.unithen.ui.error

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import de.bixilon.unithen.BuildConfig


const val CRASH_ADDRESS = "unithen-crash" + '@' + "bixilon" + '.' + "de"

@Composable
fun ReportErrorButton(stack: String) {
    var disabled by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Button({
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(CRASH_ADDRESS))
                putExtra(Intent.EXTRA_SUBJECT, "UniThen Crash")
                putExtra(Intent.EXTRA_TEXT, "Hi there,\nApp version: ${BuildConfig.VERSION}\nPlease see the exception below:\n\n${stack}\n\n\nCan you please fix this issue?\nThanks!")
            }


            context.startActivity(Intent.createChooser(intent, "Pick an Email provider"))
        } catch (error: Throwable) {
            error.printStackTrace()
        } finally {
            disabled = true
        }
    }, enabled = !disabled) {
        Icon(Icons.Filled.Report, "report")
        Text("Report")
    }
}
