package de.bixilon.unithen.ui.main.checkin.scan.qr.confirm

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.errors.NetworkException
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.theme.checkInSuccess
import de.bixilon.unithen.ui.util.useAsyncNetwork
import de.bixilon.unithen.ui.util.useHapticFeedback
import org.jetbrains.compose.resources.getString
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.error_network
import unithen.common.generated.resources.scan_error_rejected_message

@Composable
fun ColumnScope.ScanQrAwait(user: User, appointment: Appointment, setLoading: (Boolean) -> Unit, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val storage = LocalStorage.current
    val haptic = useHapticFeedback()
    val await by rememberSetting(Settings.SCAN_AWAIT_SERVER_CONFIRMATION)
    val offline by rememberSetting(Settings.SCAN_ALLOW_OFFLINE)


    val checkin = useAsyncNetwork {
        val await = await
        if (!await) onSuccess.invoke()
        setLoading.invoke(true)
        try {
            CheckInUtil.checkIn(storage, appointment, user)

            haptic.invoke(HapticFeedbackType.Confirm)
            if (await) onSuccess.invoke()
        } catch (error: NetworkException) {
            if (!offline) {
                onError.invoke(getString(Res.string.error_network, error.message ?: ""))
            } else {
                onSuccess.invoke()
            }
            throw error
        } catch (error: CheckInError) {
            haptic.invoke(HapticFeedbackType.Reject)
            onError.invoke(getString(Res.string.scan_error_rejected_message, error.error._i18n()))
        } finally {
            setLoading.invoke(false)
        }
    }

    ConfirmScreenWarning(Icons.Filled.QuestionMark, checkInSuccess, null)

    Spacer(Modifier.height(16.dp))

    DetailsContainer(user, appointment)

    Spacer(Modifier
        .weight(1.0f)
        .defaultMinSize(minHeight = 16.dp))

    if (!checkin.active) {
        ConfirmScreenActions(user, confirm = { checkin.invoke() })
    }
}
