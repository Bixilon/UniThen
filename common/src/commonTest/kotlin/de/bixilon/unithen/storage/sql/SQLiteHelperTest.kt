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

import de.bixilon.kutil.uuid.UuidUtil.toUuid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class SQLiteHelperTest {

    @Test
    fun `create in memory database`() {
        val helper = createSqliteHelper(null)


    }

    @Test
    fun `query all datatypes with get from cursor`() {
        val helper = createSqliteHelper(null)

        helper.query().use {
            val cursor = it.query("SELECT 5,10,'abc',NULL,'00000000-1111-2222-3333-444444444444' ")
            assertTrue { cursor.moveToNext() }

            assertTrue(cursor.isNull(3))

            assertEquals(5, cursor.getInt(0))
            assertEquals(10, cursor.getInt(1))

            assertEquals(5L, cursor.getLong(0))
            assertEquals(10L, cursor.getLong(1))

            assertEquals("abc", cursor.getString(2))
            assertEquals("abc", cursor.getStringOrNull(2))
            assertNull(cursor.getStringOrNull(3))

            assertEquals(Instant.fromEpochSeconds(10L), cursor.getInstant(1))
            assertEquals(Instant.fromEpochSeconds(10L), cursor.getInstantOrNull(1))
            assertNull(cursor.getInstantOrNull(3))


            assertEquals("00000000-1111-2222-3333-444444444444".toUuid(), cursor.getUUID(4))
            assertEquals("00000000-1111-2222-3333-444444444444".toUuid(), cursor.getUUIDOrNull(4))
            assertNull(cursor.getUUIDOrNull(3))

            // TODO: blob

            cursor.close()
        }
    }

    @Test
    fun `query with all datatypes as parameters`() {
        val helper = createSqliteHelper(null)

        helper.query().use {
            // TODO: byte array/blob unsupported on android
            val cursor = it.query("SELECT ?,?,?,?,?,?,?,?", "abc", 5, 10L, Instant.fromEpochSeconds(15L), "00000000-1111-2222-3333-444444444444".toUuid(), true, null)
            assertTrue { cursor.moveToNext() }

            assertEquals("abc", cursor.getString(0))
            assertEquals(5, cursor.getInt(1))
            assertEquals(10L, cursor.getLong(2))
            assertEquals(Instant.fromEpochSeconds(15L), cursor.getInstant(3))
            assertEquals("00000000-1111-2222-3333-444444444444".toUuid(), cursor.getUUID(4))
            assertEquals(1, cursor.getInt(5))
            assertTrue(cursor.isNull(6))


            cursor.close()
        }
    }
}
