package de.bixilon.unithen.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.uri.URIUtil.toURI
import de.bixilon.unithen.api.UniNowUtil
import de.bixilon.unithen.api.authentication.Authentication
import java.net.URI
import java.util.*


const val AUTHENTICATION_ROUTE = "/auth"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(base: URI = "https://kurse.zhs-muenchen.de".toURI()) = Scaffold(
    topBar = {
        TopAppBar(
            title = {
                Text("Authentication")
            }
        )
    },
) { innerPadding ->
    val modifier = Modifier.padding(innerPadding)
    var authentication: Authentication? by remember { mutableStateOf(null) }

    LaunchedEffect(authentication) {
        val authentication = authentication ?: return@LaunchedEffect

        Log.v("Auth", "Fetching user id...")
        val userId = UniNowUtil.fetchUserId(base, authentication)
        Log.v("Auth", "Found user id: $userId")
    }

    if (authentication == null) {
        WebAuthenticationView(modifier, base) { authentication = it }
        return@Scaffold
    }

    Row(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
        )
        Text("Authenticating...")
    }
}
