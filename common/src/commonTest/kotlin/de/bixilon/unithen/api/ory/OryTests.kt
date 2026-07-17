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

package de.bixilon.unithen.api.ory

class OryTests {

    @Test
    fun `parse zhs`() {
        val data = OryTests::class.java.getResourceAsStream("/ory/whoami.json")!!.readAsString()

        val parsed = Jackson.MAPPER.decodeFromString<Whoami>(data)

        assertEquals(parsed.identity.id, "00000000-1111-2222-3333-444444444444".toUuid())
        assertEquals(parsed.identity.traits.name.first, "firstname")
        assertEquals(parsed.identity.traits.name.last, "lastname")
    }
}
