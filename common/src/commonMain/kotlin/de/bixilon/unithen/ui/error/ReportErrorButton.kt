package de.bixilon.unithen.ui.error

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*


const val CRASH_ADDRESS = "unithen-crash" + '@' + "bixilon" + '.' + "de"

@Composable
expect fun useSendCrashMail(): (stack: String) -> Unit

@Composable
fun ReportErrorButton(stack: String) {
    var disabled by remember { mutableStateOf(false) }
    val send = useSendCrashMail()

    Button({
        try {
            send.invoke(stack)
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
