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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

fun create() = SqlStorage(createMemoryHelper())
suspend fun empty() = create().apply { helper.load() }
suspend fun dummy() = empty().apply { this.initializeDummy() }


class SqlStorageTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `create and initialize tables`() = runBlocking {
        val storage = create()
        storage.helper.load()

        val next = storage.query("SELECT host FROM sites WHERE id=1") { it.moveToNext() }

        assertFalse(next)
    }

    @Test
    fun `create dummy database`() = runBlocking {
        val storage = dummy()

        val host = storage.query("SELECT host FROM sites WHERE id=901") { it.moveToNext(); it.getString(0) }

        assertEquals("test.local", host)
    }

    @Test
    fun `get site by id dummy database`() = runBlocking {
        val site = dummy().sites[901]

        assertEquals("test.local", site?.host)
    }

    @Test
    fun `get event by id`() = runBlocking {
        val event = dummy().events[901]

        assertEquals("Test Event (a)", event?.name)
    }

    @Test
    fun `get user by id`() = runBlocking {
        val user = dummy().users[901]

        assertEquals("Hans", user?.firstname)
    }

    @Test
    fun `get account by id`() = runBlocking {
        val account = dummy().accounts[903]

        assertEquals("Marie", account?.firstname)
    }

    @Test
    fun `get course by id`() = runBlocking {
        val course = dummy().courses[901]

        assertEquals("First course", course?.name)
    }

    @Test
    fun `is user attendee of appointment`() = runBlocking {
        val storage = dummy()

        val appointment = storage.appointments[901]!!
        val user = storage.users[906]!!
        assertTrue(storage.users.isAttendee(appointment, user))
    }

    @Test
    fun `unreferenced course created in dummy`() = runBlocking {
        val storage = dummy()

        assertEquals("Unreferenced course", storage.courses[904]?.name)
    }

    @Test
    fun `cleanup database and remove unreferenced courses`() = runBlocking {
        val storage = dummy().apply { cleanup() }

        assertEquals("First course", storage.courses[901]?.name)
        assertNull(storage.courses[904])
    }

    @Test
    fun `clear database cache`() = runBlocking {
        val storage = dummy().apply { clearCache() }

        assertEquals("First course", storage.courses[901]?.name)

        val appointment = storage.appointments[901]!!
        val user = storage.users[906]!!
        assertFalse(storage.users.isAttendee(appointment, user))
    }

    @Test
    fun `not enrolled in empty database`() = runBlocking {
        val storage = empty()

        assertFalse(storage.courses.isEnrolled())
    }

    @Test
    fun `enrolled in dummy database`() = runBlocking {
        val storage = dummy()

        assertTrue(storage.courses.isEnrolled())
    }

    @Test
    fun `not tutor in empty database`() = runBlocking {
        val storage = empty()

        assertFalse(storage.courses.isTutor())
    }

    @Test
    fun `tutor in dummy database`() = runBlocking {
        val storage = dummy()

        assertTrue(storage.courses.isTutor())
    }

    @Test
    fun `tutor for course in dummy database`() = runBlocking {
        val storage = dummy()

        assertTrue(storage.accounts.isTutor(storage.courses[901]!!))
    }

    @Test
    fun `not tutor for course in dummy database`() = runBlocking {
        val storage = dummy()

        assertFalse(storage.accounts.isTutor(storage.courses[902]!!))
    }

    @Test
    fun `create transaction and get value`() = runBlocking {
        val storage = dummy()

        val course = storage.transaction { storage.courses[901] }

        assertEquals("First course", course?.name)
    }

    @Test
    fun `create multiple transactions and get value`() = runBlocking {
        val storage = dummy()

        val a = storage.transaction { storage.courses[901] }
        val b = storage.transaction { storage.courses[901] }
        val c = storage.transaction { storage.courses[901] }

        assertEquals("First course", a?.name)
        assertEquals("First course", b?.name)
        assertEquals("First course", c?.name)
    }

    @Test
    fun `queue take item`() = runBlocking {
        val storage = dummy()

        val now = Clock.System.now() - 5.seconds

        var item = storage.checkInQueue.take()

        assertTrue(item!!.sync!! < now)
        assertEquals(901, item.appointment)

        item = storage.checkInQueue[storage.appointments[item.appointment]!!, storage.users[item.user]!!]

        assertTrue(item!!.sync!! > now)
    }
}
