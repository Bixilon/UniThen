package de.bixilon.unithen.ui.error

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import de.bixilon.kutil.file.FileUtil.div
import de.bixilon.kutil.file.FileUtil.mkdirParent
import java.io.FileOutputStream


const val CRASH_ADDRESS = "unithen-crash" + '@' + "bixilon" + '.' + "de"

@Composable
fun ReportErrorButton(stack: String) {
    var disabled by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Button({
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(CRASH_ADDRESS))
            intent.putExtra(Intent.EXTRA_SUBJECT, "UniThen Crash")
            intent.putExtra(Intent.EXTRA_TEXT, "Hi there,\nplease see the attached exception file for the crash.\nCan you please fix this issue?\nThanks!")
            val file = Environment.getExternalStorageDirectory() / "unithen" / "exception.txt"

            file.mkdirParent()
            FileOutputStream(file).use { it.bufferedWriter().write(stack) }

            val uri = Uri.fromFile(file)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
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
