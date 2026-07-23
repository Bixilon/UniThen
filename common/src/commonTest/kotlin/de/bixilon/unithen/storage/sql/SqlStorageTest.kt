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

import de.bixilon.unithen.debug.DebugUtil.initializeDummy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlin.test.*

fun create() = SqlStorage(createMemoryHelper())
fun dummy() = create().apply { helper.load(); this.initializeDummy() }


class SqlStorageTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `create and initialize tables`() {
        val storage = create()
        storage.helper.load()

        val next = storage.helper.query("SELECT host FROM sites WHERE id=1").moveToNext()

        assertFalse(next)
    }

    @Test
    fun `create dummy database`() {
        val storage = dummy()

        val cursor = storage.helper.query("SELECT host FROM sites WHERE id=901")

        assertTrue(cursor.moveToNext())
        assertEquals("test.local", cursor.getString(0))
    }

    @Test
    fun `get site by id dummy database`() {
        val site = dummy().sites[901]

        assertEquals("test.local", site?.host)
    }

    @Test
    fun `get event by id`() {
        val event = dummy().events[901]

        assertEquals("Test Event (a)", event?.name)
    }

    @Test
    fun `get user by id`() {
        val user = dummy().users[901]

        assertEquals("Hans", user?.firstname)
    }

    @Test
    fun `get account by id`() {
        val account = dummy().accounts[903]

        assertEquals("Marie", account?.firstname)
    }

    @Test
    fun `get course by id`() {
        val course = dummy().courses[901]

        assertEquals("First course", course?.name)
    }
}
