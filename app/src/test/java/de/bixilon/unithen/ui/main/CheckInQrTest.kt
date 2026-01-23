package de.bixilon.unithen.ui.main

import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import junit.framework.TestCase.assertEquals
import org.junit.Test


class CheckInQrTest {

    @Test
    fun `generate text`() {
        val expected = """{"appointment_id":"20000000-0005-0000-0000-000000000006","user_id":"10000000-0003-0000-0000-000000000001","userName":{"last":"Last","first":"First"}}"""
        val data = createQrCode(
            "10000000-0003-0000-0000-000000000001".toUUID(),
            "20000000-0005-0000-0000-000000000006".toUUID(),
            "First",
            "Last"
        )

        assertEquals(data, expected)
    }
}
