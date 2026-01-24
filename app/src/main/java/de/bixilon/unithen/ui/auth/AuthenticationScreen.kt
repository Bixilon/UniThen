package de.bixilon.unithen.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.UniNowUtil
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import java.net.URI


fun AUTHENTICATION_ROUTE(site: Site) = "/auth/${site.id}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(base: URI) = Scaffold(
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

        Log.v("Auth", "Fetching user details...")
        val details = UniNowUtil.fetchUserDetails(base, authentication)
        Log.v("Auth", "Found user details: $details")

        DataStorage.STORAGE.transaction {
            //   val site = it.sites.getSite(base)
            //   it.updateAccount(site, details, authentication)
        }
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
