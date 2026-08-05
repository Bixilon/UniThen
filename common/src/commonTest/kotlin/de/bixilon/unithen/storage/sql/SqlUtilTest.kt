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

package de.bixilon.unithen.storage.sql

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SqlUtilTest {

    @Test
    fun `single statement stays single statement`() {
        val raw = "SELECT * FROM a;"
        val split = SqlUtil.split(raw)

        assertEquals(listOf("SELECT * FROM a"), split)
    }

    @Test
    fun `split multiple statements same line`() {
        val raw = "SELECT * FROM a; SELECT * FROM b; SELECT * FROM c;"
        val split = SqlUtil.split(raw)

        assertEquals(listOf("SELECT * FROM a", "SELECT * FROM b", "SELECT * FROM c"), split)
    }

    @Test
    fun `split multiple statements multiple lines`() {
        val raw = "SELECT * FROM a;\nSELECT * FROM b;\nSELECT * FROM c;"
        val split = SqlUtil.split(raw)

        assertEquals(listOf("SELECT * FROM a", "SELECT * FROM b", "SELECT * FROM c"), split)
    }

    @Test
    fun `not splitting begin statements`() {
        val a = "SOMETHING\n" +
                "BEGIN\n" +
                "BLOCK;\n" +
                "END"
        val b = "OTHER\n" +
                "BEGIN\n" +
                "BLOCK2;\n" +
                "END"

        val raw = "$a;\n$b;"
        val split = SqlUtil.split(raw)


        assertEquals(listOf(a, b), split)
    }

    @Test
    fun `crash on trailing data`() {
        val raw = "SELECT * FROM a"

        assertFails { SqlUtil.split(raw) }
    }
}
