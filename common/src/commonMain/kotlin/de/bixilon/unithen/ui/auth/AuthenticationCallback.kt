package de.bixilon.unithen.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetchFromCourses
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.main.MainScreens
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useAsyncNetwork
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import unithen.common.generated.resources.*


@Composable
fun AuthenticationCallback(site: Site, authentication: Authentication) {
    val storage = LocalStorage.current
    var entrypoint by rememberSetting(Settings.ENTRYPOINT, MainScreens)

    var message by remember { mutableStateOf(runBlocking { getString(Res.string.authentication_fetching_user_details) }) }


    val fetch = useAsyncNetwork<Unit>(null) {
        val first = storage.accounts.count == 0
        val api = AuthenticatedUniNowApi(site.host, authentication)
        val details = api.getUserDetails()

        val account = storage.transaction { it.accounts.add(site, details, authentication) }

        message = getString(Res.string.authentication_course_list)

        storage.fetchFromCourses(account, true) { message = runBlocking { getString(Res.string.authentication_fetching, it.course, it.courses) } }

        when {
            !first -> Unit
            storage.courses.isTutor() -> entrypoint = MainScreens.CHECKIN_SCAN
            storage.courses.isNotTutor() -> entrypoint = MainScreens.CHECKIN_PRESENT
        }

        // TODO: callback.invoke()
    }

    LaunchedEffect(Unit) { fetch.invoke(Unit) }

    AlertDialog(
        confirmButton = {},
        onDismissRequest = {},
        title = { Text(Res.string.authentication_loading.i18n()) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
                Text(Res.string.authentication_take_a_while.i18n(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}
