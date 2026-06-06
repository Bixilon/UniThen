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

import de.bixilon.unithen.util.Jackson
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.uuid.Uuid


class ScannedQrCodeTest {

    @Test
    fun `read scanned data no username`() {
        val text = """{"appointment_id": "2efdc1bd-5963-43cf-b3b5-df5aa092cff2", "user_id": "2efdc1bd-5963-43cf-b3b5-df5aa092cff2"}"""

        val read = Jackson.MAPPER.decodeFromString<ScannedQrCode>(text)

        assertEquals(read, ScannedQrCode(Uuid.parse("2efdc1bd-5963-43cf-b3b5-df5aa092cff2"), Uuid.parse("2efdc1bd-5963-43cf-b3b5-df5aa092cff2")))
    }

    @Test
    fun `read scanned data with username`() {
        val text = """{"appointment_id":"2efdc1bd-5963-43cf-b3b5-df5aa092cff2","user_id":"2efdc1bd-5963-43cf-b3b5-df5aa092cff2","userName":{"last":"Last","first":"First"}}"""

        val read = Jackson.MAPPER.decodeFromString<ScannedQrCode>(text)

        assertEquals(read, ScannedQrCode(Uuid.parse("2efdc1bd-5963-43cf-b3b5-df5aa092cff2"), Uuid.parse("2efdc1bd-5963-43cf-b3b5-df5aa092cff2")))
    }
}
