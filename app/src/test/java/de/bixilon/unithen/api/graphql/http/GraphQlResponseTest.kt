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

package de.bixilon.unithen.api.graphql.http

import de.bixilon.unithen.util.Jackson
import junit.framework.TestCase.assertEquals
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class GraphQlResponseTest {

    private inline fun <reified T> read(data: String) = Jackson.GRAPHQL.decodeFromString<GraphQlResponse<T>>(data)

    @Test
    fun `read string`() {
        val data = """{"data": "test"}"""

        val read = read<String>(data)
        val expected = GraphQlResponse("test")

        assertEquals(read, expected)
    }

    @Test
    fun `write string`() {
        val request = GraphQlRequest("query something", JsonObject(mapOf("hello" to JsonPrimitive("world"))))

        val string = Jackson.GRAPHQL.encodeToString(request)

        assertEquals(string, """{"query":"query something","variables":{"hello":"world"}}""")
    }
}
