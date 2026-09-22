package de.bixilon.unithen.ui.loader

import androidx.compose.runtime.*
import de.bixilon.unithen.ui.containers.LoadingScreen
import de.bixilon.unithen.ui.error.CrashScreen
import de.bixilon.unithen.ui.icons.Logo
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.DelayedContent
import de.bixilon.unithen.ui.util.i18n
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.loading_database
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun DatabaseLoadingScreen(content: @Composable () -> Unit) {
    val storage = LocalStorage.current

    var error by remember { mutableStateOf<Throwable?>(null) }
    var active by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            withContext(Dispatchers.IO) {
                storage.helper.load()
            }
        } catch (thrown: Throwable) {
            thrown.printStackTrace()
            error = thrown
        } finally {
            active = false
        }
    }

    if (active) {
        DelayedContent(100.milliseconds) {
            LoadingScreen(Res.string.loading_database.i18n(), Logo)
        }
        return
    }

    error?.let { CrashScreen("Error during database loading", it); return }

    content.invoke()
}
