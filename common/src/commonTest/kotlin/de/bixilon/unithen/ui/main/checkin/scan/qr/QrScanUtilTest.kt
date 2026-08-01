package de.bixilon.unithen.ui.main.checkin.scan.qr

import de.bixilon.unithen.storage.sql.dummy
import de.bixilon.unithen.ui.main.checkin.scan.qr.types.ScannedQrCodeV1
import de.bixilon.unithen.util.Kutil.toUuid
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QrScanUtilTest {

    private fun scan(user: String, appointment: String = "00000000-0000-0000-0000-000000000001"): QrScanResult = runBlocking {
        val storage = dummy()

        return@runBlocking QrScanUtil.scan(storage, storage.appointments["00000000-0000-0000-0000-000000000001".toUuid()], ScannedQrCodeV1(appointment.toUuid(), user.toUuid()))
    }

    @Test
    fun `scan valid ticket`() = runBlocking {
        val result = scan("00000000-0000-0000-0000-000000000002")

        assertTrue { result is QrScanResult.Accepted }
        result as QrScanResult.Accepted

        assertEquals("00000000-0000-0000-0000-000000000002".toUuid(), result.user.uuid)
        assertEquals("00000000-0000-0000-0000-000000000001".toUuid(), result.appointment.uuid)
    }

    @Test
    fun `scan already checked in`() = runBlocking {
        val result = scan("00000000-0000-0000-0000-000000000006")

        assertTrue { result is QrScanResult.AlreadyCheckedIn }
    }

    @Test
    fun `scan checkin pending`() = runBlocking {
        val result = scan("00000000-0000-0000-0000-000000000011")

        assertTrue { result is QrScanResult.CheckInPending }
    }

    @Test
    fun `scan checkout pending`() = runBlocking {
        val result = scan("00000000-0000-0000-0000-000000000007")

        assertTrue { result is QrScanResult.CheckOutPending }
    }

    @Test
    fun `scan different appointment`() = runBlocking {
        val result = scan("00000000-0000-0000-0000-000000000001", "00000000-0000-0000-0000-000000000004")

        assertTrue { result is QrScanResult.WrongAppointment }
    }

    @Test
    fun `scan different course`() = runBlocking {
        val result = scan("00000000-0000-0000-0000-000000000005", "00000000-0000-0000-0000-000000000003")

        assertTrue { result is QrScanResult.WrongCourse }
    }

    @Test
    fun `scan not enrolled`() = runBlocking {
        val result = scan("00000000-0000-0000-0000-000000000001")

        assertTrue { result is QrScanResult.NotEnrolled }
    }

    @Test
    fun `scan server rejected`() = runBlocking {
        val result = scan("00000000-0000-0000-0000-000000000004")

        assertTrue { result is QrScanResult.Rejected }
    }

    @Test
    fun `scan invalid user`() = runBlocking {
        val result = scan("10000000-0000-0000-0000-000000000004")

        assertTrue { result is QrScanResult.UnknownUser }
    }

    @Test
    fun `scan invalid appointment`() = runBlocking {
        val result = scan("00000000-0000-0000-0000-000000000001", "10000000-0000-0000-0000-000000000001")

        assertTrue { result is QrScanResult.InvalidAppointment }
    }
}
