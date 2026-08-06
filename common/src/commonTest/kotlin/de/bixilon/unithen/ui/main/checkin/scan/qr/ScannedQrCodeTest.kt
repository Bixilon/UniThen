/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.ui.main.checkin.scan.qr

import de.bixilon.kutil.uuid.UuidUtil.toUuid
import de.bixilon.unithen.ui.main.checkin.scan.qr.types.ScannedQrCodeV1
import de.bixilon.unithen.ui.main.checkin.scan.qr.types.ScannedQrCodeV2
import kotlin.test.Test
import kotlin.test.assertEquals


class ScannedQrCodeTest {
    val expectedV1 = ScannedQrCodeV1("2efdc1bd-5963-43cf-b3b5-df5aa092cff2".toUuid(), "5f14e88d-affd-4f42-9e22-f4c5279b17b7".toUuid())
    val expectedV2 = ScannedQrCodeV2("2efdc1bd-5963-43cf-b3b5-df5aa092cff2".toUuid(), "5f14e88d-affd-4f42-9e22-f4c5279b17b7".toUuid())

    @Test
    fun `write v1 without name`() {
        val expected = """{"appointment_id":"2efdc1bd-5963-43cf-b3b5-df5aa092cff2","user_id":"5f14e88d-affd-4f42-9e22-f4c5279b17b7"}"""

        assertEquals(expected, expectedV1.encode())
    }

    @Test
    fun `read v1 without name`() {
        val text = """{"appointment_id": "2efdc1bd-5963-43cf-b3b5-df5aa092cff2", "user_id": "5f14e88d-affd-4f42-9e22-f4c5279b17b7"}"""

        val read = ScannedQrCodeV1.decode(text)

        assertEquals(read, expectedV1)
    }

    @Test
    fun `read v1 with name`() {
        val text = """{"appointment_id":"2efdc1bd-5963-43cf-b3b5-df5aa092cff2","user_id":"5f14e88d-affd-4f42-9e22-f4c5279b17b7","userName":{"last":"Last","first":"First"}}"""

        val read = ScannedQrCodeV1.decode(text)

        assertEquals(read, expectedV1)
    }

    @Test
    fun `write v2`() {
        val actual = expectedV2.encode()
        val expected = """UTV2E.57MONDBYP8FWMSASLDK:CQ+0C/HT8BM+0AR:JL.UE05-:2"""

        assertEquals(expected, actual)
    }

    @Test
    fun `read v2`() {
        val expected = ScannedQrCodeV2.decode("""UTV2E.57MONDBYP8FWMSASLDK:CQ+0C/HT8BM+0AR:JL.UE05-:2""")

        assertEquals(expected, expectedV2)
    }
}
