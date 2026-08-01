package de.bixilon.unithen.ui.auth.ory

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.string.WhitespaceUtil.removeWhitespaces
import de.bixilon.unithen.api.authentication.OryTokenAuthentication
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.AuthenticationCallbackRoute
import de.bixilon.unithen.ui.main.AuthenticationRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.state.rememberStateOf
import de.bixilon.unithen.ui.util.useAsyncNetwork


@Composable
fun EmailAuthenticationScreen(site: Site, config: OryConfig) {
    val navigation = LocalNavigation.current
    var password by rememberStateOf { "" }
    var email by rememberStateOf { "" }

    var error by rememberStateOf<String?> { null }

    val auth = useAsyncNetwork {
        try {
            val token = config.loginEmail(email, password)
            navigation.popIf { it is AuthenticationRoute }
            navigation.navigate(AuthenticationCallbackRoute(site, OryTokenAuthentication(token.sessionToken)))
            // TODO: delete login flow
        } catch (exception: InvalidCredentialException) {
            error = exception.message
        }
    }

    Screen {
        ScreenTitle("Login")
        Text("Please login with your email and password:")

        Spacer(Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it.removeWhitespaces().lowercase() },
            label = { Text("E-Mail") },
            singleLine = true,
            placeholder = { Text("E-Mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            placeholder = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.weight(1.0f))

        val disabled = password.isBlank() || '@' !in email || auth.active

        Button({ auth.invoke() }, enabled = !disabled, modifier = Modifier.fillMaxWidth()) {
            if (auth.active) CircularProgressIndicator() else Text("Login")
        }
    }
}
