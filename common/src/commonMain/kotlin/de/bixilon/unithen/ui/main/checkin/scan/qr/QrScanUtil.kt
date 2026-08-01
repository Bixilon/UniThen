package de.bixilon.unithen.ui.main.checkin.scan.qr

import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.qr.types.ScannedQrCode
import de.bixilon.unithen.ui.main.checkin.scan.qr.types.ScannedQrCodeV1
import de.bixilon.unithen.ui.main.settings.types.Labeled
import org.jetbrains.compose.resources.StringResource
import unithen.common.generated.resources.*
import kotlin.uuid.Uuid

data class Ignore(
    val appointment: Uuid,
    val user: Uuid,
)

sealed interface QrScanResult {

    data class Accepted(val appointment: Appointment, val user: User) : QrScanResult

    sealed class Error(override val label: StringResource) : QrScanResult, Labeled
    sealed class SoftError(label: StringResource, val userId: Uuid) : Error(label) {
        abstract val appointment: Appointment
    }


    data class UnknownUser(override val appointment: Appointment, val user: Uuid) : SoftError(Res.string.scan_error_unknown_user, user)

    data class NotEnrolled(override val appointment: Appointment, val user: User) : SoftError(Res.string.scan_error_not_enrolled, user.uuid)
    data class AlreadyCheckedIn(override val appointment: Appointment, val user: User) : SoftError(Res.string.scan_error_already_checked_in, user.uuid)
    data class CheckInPending(override val appointment: Appointment, val user: User) : SoftError(Res.string.scan_error_check_in_pending, user.uuid)
    data class Rejected(override val appointment: Appointment, val user: User, val message: String) : SoftError(Res.string.scan_unknown_error_server_generic, user.uuid)
    data class CheckOutPending(override val appointment: Appointment, val user: User) : SoftError(Res.string.scan_error_check_out_pending, user.uuid)

    object InvalidFormat : Error(Res.string.scan_error_invalid_format)
    object InvalidAppointment : Error(Res.string.scan_error_invalid_appointment)

    data class WrongAppointment(val appointment: Appointment) : Error(Res.string.scan_error_wrong_appointment)
    data class WrongCourse(val course: Course) : Error(Res.string.scan_error_invalid_course)

    object Other : Error(Res.string.scan_error_other)
}

object QrScanUtil {

    fun scan(storage: SqlStorage, appointment: Appointment, user: User): QrScanResult {
        val course = storage.courses[appointment.course]!!

        val enrolled = storage.users.isEnrolled(course, user)
        if (!enrolled) return QrScanResult.NotEnrolled(appointment, user)

        val attendee = storage.users.isAttendee(appointment, user)
        if (attendee) return QrScanResult.AlreadyCheckedIn(appointment, user)

        val attempt = storage.checkInQueue[appointment, user]
        if (attempt != null) {
            if (attempt.attempt != null) return QrScanResult.CheckOutPending(appointment, user)
            if (attempt.message != null) return QrScanResult.Rejected(appointment, user, attempt.message)

            return QrScanResult.CheckInPending(appointment, user)
        }

        return QrScanResult.Accepted(appointment, user)
    }

    fun scan(storage: SqlStorage, appointment: Appointment, userId: Uuid): QrScanResult {
        val course = storage.courses[appointment.course]!!

        val site = storage.sites[course.site]!!
        val user = storage.users[site, userId] ?: return QrScanResult.UnknownUser(appointment, userId)


        return scan(storage, appointment, user)
    }

    fun scan(storage: SqlStorage, appointments: List<Appointment>, userId: Uuid, appointmentId: Uuid): QrScanResult {
        val appointment = appointments.find { it.uuid == appointmentId }
        if (appointment == null) {
            val actual = storage.appointments[appointmentId]
            if (actual.isEmpty()) return QrScanResult.InvalidAppointment
            if (actual.size > 1) return QrScanResult.Other

            val course = storage.courses[actual.first().course]!!

            appointments.find { it.course == course.id } ?: return QrScanResult.WrongCourse(course)

            return QrScanResult.WrongAppointment(actual.first())
        }

        return scan(storage, appointment, userId)
    }

    fun scan(storage: SqlStorage, appointments: List<Appointment>, code: ScannedQrCode): QrScanResult {
        return when (code) {
            is ScannedQrCodeV1 -> scan(storage, appointments, code.userId, code.appointmentId)
        }
    }
}
