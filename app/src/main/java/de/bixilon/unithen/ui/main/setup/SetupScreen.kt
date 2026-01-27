package de.bixilon.unithen.ui.main.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.bixilon.unithen.ui.main.AddAccountRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation


@Composable
fun SetupScreen() {
    Column {
        Text("Setup")

        Text("This App is not affiliated with UniNow GmbH!")

        Text("Your credentials will be saved on this device")
        Text("The licence will still be determined, but for now the app creator is not responsible for any damage to your account. This app comes with absolutely NO warranty!")

        val navigation = LocalNavigation.current
        Button({ navigation.pop(); navigation.navigate(AddAccountRoute) }) {
            Text("Got it, let me add my account!")
        }
    }
}
