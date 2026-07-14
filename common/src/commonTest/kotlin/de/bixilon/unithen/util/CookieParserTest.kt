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

package de.bixilon.unithen.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CookieParserTest {

    @Test
    fun `no cookies`() {
        val cookies = CookieParser.parse("")
        assertEquals(cookies, emptyMap<String, String>())
    }

    @Test
    fun `single cookie`() {
        val cookies = CookieParser.parse("name=value")
        assertEquals(cookies, mapOf("name" to "value"))
    }

    @Test
    fun `multiple cookie`() {
        val cookies = CookieParser.parse("name=value; second=true")
        assertEquals(cookies, mapOf("name" to "value", "second" to "true"))
    }

    @Test
    fun `case insensitive cookie`() {
        val cookies = CookieParser.parse("nAme=value; secOnd=True")
        assertEquals(cookies, mapOf("name" to "value", "second" to "True"))
    }
}
