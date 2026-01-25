package de.bixilon.unithen.ui.auth

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.UniNowUtil
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import java.net.URI


enum class AuthenticationState {
    FETCH_USER_DETAILS,
    FETCH_COURSES,
    DONE,
}

@Composable
fun AuthenticationProgress(text: String, modifier: Modifier) {
    Row(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
        )
        Text("Fetching courses...")
    }
}

private fun fetchUserDetails(base: URI, authentication: Authentication, callback: (state: AuthenticationState) -> Unit) {
    Log.i("Auth", "Fetching user details...")
    val details = UniNowUtil.fetchUserDetails(base, authentication)

    Log.v("Auth", "Found user details: $details")

    var site: Site = unsafeNull()
    var account: Account = unsafeNull()

    DataStorage.STORAGE.transaction {
        site = it.sites[base]!!
        account = it.accounts.add(site, details, authentication)
    }

    callback.invoke(AuthenticationState.FETCH_COURSES)

    Log.i("Auth", "Fetching courses...")
    val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(account.session))
    val courses = api.postings(account.uuid)

    DataStorage.STORAGE.populate(site, account, courses)
    Log.i("Auth", "Courses fetched (total: ${courses.size})")
    callback.invoke(AuthenticationState.DONE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(base: URI, callback: (Authentication) -> Unit) = Scaffold(
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
    var state by remember { mutableStateOf(AuthenticationState.FETCH_USER_DETAILS) }
    var error: Throwable? by remember { mutableStateOf(null) }

    LaunchedEffect(state) {
        if (state != AuthenticationState.FETCH_USER_DETAILS) return@LaunchedEffect
        val authentication = authentication ?: return@LaunchedEffect

        DefaultThreadPool += add@{
            try {
                fetchUserDetails(base, authentication) { state = it }
                callback.invoke(authentication)
            } catch (_error: Throwable) {
                Log.e("Auth", "Error fetching user details: $_error")
                _error.printStackTrace()
                error = _error
                return@add
            }
        }
    }

    if (error != null) {
        Text("Error fetching user details: $error", modifier = modifier)
        return@Scaffold
    }

    if (authentication == null) {
        WebAuthenticationView(modifier, base) { authentication = it }
        return@Scaffold
    }


    when (state) {
        AuthenticationState.FETCH_USER_DETAILS -> AuthenticationProgress("Fetching user details...", modifier)
        AuthenticationState.FETCH_COURSES -> AuthenticationProgress("Fetching courses...", modifier)
        AuthenticationState.DONE -> Text("Done!", modifier = modifier)
    }
}
