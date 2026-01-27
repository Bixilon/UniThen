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
