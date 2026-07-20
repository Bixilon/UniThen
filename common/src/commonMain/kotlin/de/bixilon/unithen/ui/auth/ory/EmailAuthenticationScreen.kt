package de.bixilon.unithen.ui.auth.ory

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.util.useAsyncNetwork


@Composable
fun EmailAuthenticationScreen(config: OryConfig) {
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val auth = useAsyncNetwork<Unit>(null) { config.loginEmail(email, password) } // TODO: handle error, callback screen

    Screen {
        ScreenTitle("Login")
        Text("Please login with your email and password:")

        TextField(
            value = email,
            onValueChange = { email = it },
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

        Text("Forgot password?") // TODO: open /auth/recovery

        val disabled = password.isBlank() || '@' !in email || auth.active

        Button({ auth.invoke(Unit) }, enabled = !disabled, modifier = Modifier.fillMaxWidth()) {
            if (auth.active) CircularProgressIndicator() else Text("Login")
        }
    }
}
