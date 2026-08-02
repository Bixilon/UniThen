package de.bixilon.unithen.ui.main.about

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.BuildInfo
import de.bixilon.unithen.api.HttpUtil
import de.bixilon.unithen.http.CLIENT
import de.bixilon.unithen.ui.util.useAsyncNetwork
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


@Composable
fun UpdateCheckButton() {
    val handler = LocalUriHandler.current
    var next by remember { mutableIntStateOf(-1) }

    LaunchedEffect(next) {
        if (next > 0 && next > BuildInfo.VERSION_CODE) {
            handler.openUri("https://gitlab.bixilon.de/bixilon/unithen/-/releases")
        }
    }

    val check = useAsyncNetwork {
        val request = HttpUtil.create("gitlab.bixilon.de", "/bixilon/unithen/-/raw/master/fdroid.txt").apply { method = HttpMethod.Get }

        val response = CLIENT.request(request)

        if (response.status != HttpStatusCode.OK) throw IllegalStateException("Request is not OK")

        // Same regex as for fdroid: https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/de.bixilon.unithen.yml
        next = Regex("^(\\d+)$", RegexOption.MULTILINE).find(response.bodyAsText())!!.groups[1]!!.value.toInt()
    }


    Button({ check.invoke() }, enabled = !check.active && next < 0) {
        Icon(Icons.Default.Update, "")
        Spacer(Modifier.width(8.dp))
        Text(when {
            check.active -> "Checking for updates..."
            next > 0 && next <= BuildInfo.VERSION_CODE -> "No update available!"
            next > BuildInfo.VERSION_CODE -> "Update available!"
            else -> "Check for updates"
        })
    }
}
