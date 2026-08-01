package de.bixilon.unithen.ui.main.checkin.scan.qr.overlays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.ui.main.ScanQrConfirmRoute
import de.bixilon.unithen.ui.main.checkin.scan.qr.QrScanResult
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.util.state.rememberDelayedState
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@Composable
fun rememberDelayedOverlay(): MutableState<QrScanResult.SoftError?> {
    val auto by rememberSetting(Settings.SCAN_QR_AUTO_SCAN)
    val navigation = LocalNavigation.current


    return rememberDelayedState(1.seconds, 300.milliseconds) {
        if (!auto) navigation.pop()
        navigation.navigate(ScanQrConfirmRoute(it.appointment, it.userId))
    }
}
