package de.bixilon.unithen.ui.main.checkin.scan.errors

import androidx.compose.runtime.Composable
import de.bixilon.unithen.ui.main.settings.types.Labeled
import de.bixilon.unithen.ui.util.i18n
import org.jetbrains.compose.resources.getString
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.scan_error_not_approved


sealed interface CheckInErrors {
    val message: String

    @Composable
    fun i18n() = if (this is Labeled) this.label.i18n() else this.message

    suspend fun _i18n() = if (this is Labeled) getString(this.label) else this.message

    object CheckInClosed : CheckInErrors {
        override val message = "checkin_closed"
    }

    object NotApproved : CheckInErrors, Labeled {
        override val message = "not_approved"
        override val label get() = Res.string.scan_error_not_approved
    }

    object AlreadyCheckedIn : CheckInErrors {
        override val message = "already_checked_in"
    }

    object Unknown : CheckInErrors {
        override val message get() = "unknown"
    }

    data class Other(override val message: String) : CheckInErrors

    // TODO: unknown user, not enrolled, more?


    companion object {

        fun of(message: String?) = when (message?.trim()?.lowercase()) {
            null, "" -> null
            "checkin closed", CheckInClosed.message -> CheckInClosed
            "booking not approved yet", NotApproved.message -> NotApproved
            "already checked in", AlreadyCheckedIn.message -> AlreadyCheckedIn
            else -> null
        }
    }
}
