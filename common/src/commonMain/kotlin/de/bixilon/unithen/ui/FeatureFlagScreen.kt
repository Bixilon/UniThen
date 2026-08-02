package de.bixilon.unithen.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.bixilon.unithen.settings.FeatureFlags
import de.bixilon.unithen.settings.QrVersion
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.settings.types.EnumSetting

@Composable
fun FeatureFlagScreen() {
    Screen {
        ScreenTitle("Feature flags")
        Text("All those options might be removed any time or completely break the app! Only use if you know what you are doing!!!", color = Color.Red)

        EnumSetting(FeatureFlags.QR_VERSION, QrVersion, "QR code version", "Scanning might break with the offical app")
    }
}
