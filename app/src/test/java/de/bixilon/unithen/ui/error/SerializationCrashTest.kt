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

package de.bixilon.unithen.ui.error

import kotlinx.serialization.SerializationException
import org.junit.Test
import kotlin.test.assertEquals

class SerializationCrashTest {

    @Test
    fun `correctly censor fields`() {
        val input = """{"id": "5b198cdd-75df-4479-b9e6-6382f61de68f", "first_name": "Moritz", "last_name": "Zwerger", "name": "Something"}"""
        val expected = """{"id": "5b198cdd-...", "first_name": "...", "last_name": "...", "name": "Som..."}"""

        val error = SerializationCrash(input, SerializationException())


        assertEquals(error.message, expected)
    }
}
