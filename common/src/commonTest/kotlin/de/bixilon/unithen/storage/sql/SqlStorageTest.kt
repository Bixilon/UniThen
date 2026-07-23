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
import kotlin.test.assertFalse


class SqlStorageTest {

    private fun create() = SqlStorage(createMemoryHelper())

    @Test
    fun `create and initialize tables`() {
        val storage = create()
        storage.helper.load()

        val next = storage.helper.query("SELECT host FROM sites WHERE id=1").moveToNext()

        assertFalse(next)
    }
}
