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

package de.bixilon.unithen.storage.sql.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class SqlFilterTest {

    @Test
    fun `AND empty filter`() {
        val filter = SqlFilter.and()
        val expected = SqlFilter("", listOf())

        assertEquals(filter, expected)
    }

    @Test
    fun `AND single filter`() {
        val filter = SqlFilter.and("abc" to "def")
        val expected = SqlFilter("abc=?", listOf("def"))

        assertEquals(filter, expected)
    }

    @Test
    fun `AND multiple filters`() {
        val filter = SqlFilter.and("abc" to "def", "xyz" to null, "ush" to 1)
        val expected = SqlFilter("abc=? AND ush=?", listOf("def", 1))

        assertEquals(filter, expected)
    }

    @Test
    fun `OR multiple filters`() {
        val filter = SqlFilter.or("abc" to "def", "xyz" to null, "ush" to 1)
        val expected = SqlFilter("abc=? OR ush=?", listOf("def", 1))

        assertEquals(filter, expected)
    }
}
