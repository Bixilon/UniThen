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
import de.bixilon.unithen.api.UserDetails
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.storage.Site
import java.net.URI


enum class AuthenticationState {
    SHOW_LOGIN,
    FETCH_USER_DETAILS,
    FETCH_COURSES,
    DONE,
}

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
    var state by remember { mutableStateOf(AuthenticationState.SHOW_LOGIN) }
    var error: Throwable? by remember { mutableStateOf(null) }

    LaunchedEffect(state) {
        if (state != AuthenticationState.FETCH_USER_DETAILS) return@LaunchedEffect
        val authentication = authentication ?: return@LaunchedEffect

        DefaultThreadPool += add@{
            Log.i("Auth", "Fetching user details...")
            val details: UserDetails
            try {
                details = UniNowUtil.fetchUserDetails(base, authentication)
            } catch (_error: Throwable) {
                Log.e("Auth", "Error fetching user details: $_error")
                _error.printStackTrace()
                error = _error
                return@add
            }
            Log.v("Auth", "Found user details: $details")

            var site: Site = unsafeNull()
            var account: Account = unsafeNull()
            DataStorage.STORAGE.transaction {
                site = it.sites[base]!!
                account = it.accounts.add(site, details, authentication)
            }

            state = AuthenticationState.FETCH_COURSES
            DefaultThreadPool += add@{
                Log.i("Auth", "Fetching courses...")
                try {
                    val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(account.session))
                    val courses = api.postings(account.uuid)

                    DataStorage.STORAGE.populate(site, account, courses)
                    Log.i("Auth", "Courses fetched (total: ${courses.size})")
                    state = AuthenticationState.DONE
                } catch (_error: Throwable) {
                    Log.e("Auth", "Error fetching courses: $_error")
                    _error.printStackTrace()
                    error = _error
                    return@add
                }
            }
        }
    }

    if (error != null) {
        Text("Error fetching user details: $error", modifier = modifier)
        return@Scaffold
    }

    when (state) {
        // TODO: WebAuthenticationView
        AuthenticationState.SHOW_LOGIN -> WebAuthenticationView(modifier, base) { authentication = it; state = AuthenticationState.FETCH_USER_DETAILS }
        AuthenticationState.FETCH_USER_DETAILS -> Row(modifier = modifier) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
            )
            Text("Fetching user details...")
        }

        AuthenticationState.FETCH_COURSES -> Row(modifier = modifier) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
            )
            Text("Fetching courses...")
        }

        AuthenticationState.DONE -> Text("Done!", modifier = modifier)
    }
}
