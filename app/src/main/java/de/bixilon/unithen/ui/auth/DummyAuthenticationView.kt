package de.bixilon.unithen.ui.auth

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.authentication.CookieAuthentication
import java.net.URI


@Composable
fun DummyAuthenticationView(modifier: Modifier = Modifier, base: URI, callback: (Authentication) -> Unit) {
    Button({ callback.invoke(CookieAuthentication("dummy")) }, modifier = modifier) {
        Text("Authenticate (dummy)")
    }
}
