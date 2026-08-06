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

import de.bixilon.kutil.string.WhitespaceUtil.removeWhitespaces
import de.bixilon.kutil.uuid.UuidUtil.toUuid
import de.bixilon.unithen.debug.DebugUtil.initializeDummy
import de.bixilon.unithen.storage.StorageTestUtil.account
import de.bixilon.unithen.storage.StorageTestUtil.appointment
import de.bixilon.unithen.storage.StorageTestUtil.course
import de.bixilon.unithen.storage.StorageTestUtil.event
import de.bixilon.unithen.storage.StorageTestUtil.site
import de.bixilon.unithen.ui.main.checkin.scan.attendees.AttendeeSort
import de.bixilon.unithen.ui.main.checkin.scan.attendees.Order
import de.bixilon.unithen.util.TestUtil.assertMatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import unithen.common.generated.resources.Res
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

fun create() = SqlStorage(createSqliteHelper())
suspend fun empty() = create().apply { helper.load() }
suspend fun dummy() = empty().apply { this.initializeDummy() }

expect fun ByteArray.copyTo(path: String)
expect fun delete(path: String)

@OptIn(ExperimentalCoroutinesApi::class)
class SqlStorageTest {

    init {
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
    fun `migrate v1 database`() {
        val original = runBlocking { Res.readBytes("files/sqlite/v1.sqlite") }
        val name = "migrate-${Random.nextInt().absoluteValue}.sqlite"
        original.copyTo(name)
        try {
            val storage = SqlStorage(createSqliteHelper(name))
            try {
                runBlocking { storage.helper.load() }

                assertEquals("Room 332", storage.appointments["801cd6fd-220b-40cb-8ebb-f4748d205c8c".toUuid()][0].location)
                assertEquals("User15", storage.accounts.get(uuid = "490e4d29-c62b-4a60-994f-bedf61f8ecb2".toUuid())[0].firstname)
            } finally {
                storage.close()
            }
        } finally {
            delete(name)
        }
    }

    @Test
    fun `ensure all sql files end with a column`() {

        fun assert(file: String) = runBlocking {
            val migration = Res.readBytes("files/sql/$file").decodeToString().removeWhitespaces().replace("\n", "")

            assertEquals(';', migration.last())
        }

        for (migration in 2..SqlStorage.VERSION) {
            assert("migrations/$migration.sql")
        }

        assert("schema.sql")
        assert("cleanup.sql")
        assert("clear_cache.sql")
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

    @Test
    fun `helper return auto correct id`() = runBlocking {
        val storage = empty()

        val site = storage.site()

        assertEquals(1, site.id)
    }

    @Test
    fun `create account and add to course`() = runBlocking {
        val storage = empty()

        val site = storage.site()
        val account = storage.account(site)
        val course = storage.course(storage.event(site))

        storage.accounts.addToCourse(account, course, false)

        assertTrue { storage.courses.isEnrolled() }
        assertFalse { storage.courses.isTutor() }
    }

    @Test
    fun `get enrolled appointments between time`() = runBlocking {
        val storage = empty()

        val site = storage.site()
        val account = storage.account(site)
        val course = storage.course(storage.event(site))

        storage.accounts.addToCourse(account, course, false)

        val start = Clock.System.now()
        val end = Clock.System.now() + 1.hours

        val uuid = Uuid.random()

        storage.appointment(course, uuid = uuid, start = start, end = end)

        var appointments = storage.appointments.getInRange(start - 1.minutes, start + 1.minutes, tutor = false)
        assertEquals(uuid, appointments[0].uuid)

        appointments = storage.appointments.getInRange(start - 1.minutes, start + 1.minutes, tutor = true)
        assertEquals(0, appointments.size)

        appointments = storage.appointments.getInRange(start - 3.minutes, start - 1.minutes)
        assertEquals(0, appointments.size)
    }

    @Test
    fun `get tutor appointments between time`() = runBlocking {
        val storage = empty()

        val site = storage.site()
        val account = storage.account(site)
        val course = storage.course(storage.event(site))

        storage.accounts.addToCourse(account, course, true)

        val start = Clock.System.now()
        val end = Clock.System.now() + 1.hours

        val uuid = Uuid.random()

        storage.appointment(course, uuid = uuid, start = start, end = end)

        var appointments = storage.appointments.getInRange(start - 1.minutes, start + 1.minutes, tutor = true)
        assertEquals(uuid, appointments[0].uuid)

        appointments = storage.appointments.getInRange(start - 1.minutes, start + 1.minutes, tutor = false)
        assertEquals(0, appointments.size)

        appointments = storage.appointments.getInRange(start - 3.minutes, start - 1.minutes)
        assertEquals(0, appointments.size)
    }

    @Test
    fun `get attendees without search`() = runBlocking {
        val storage = dummy()
        val appointment = storage.appointments[901]!!
        val attendees = storage.users.getAttendees(appointment, "", AttendeeSort.LASTNAME, Order.ASC)

        assertEquals(listOf(906), attendees.map { it.id })
    }

    @Test
    fun `get attendees with search`() = runBlocking {
        val storage = dummy()
        val appointment = storage.appointments[901]!!

        var attendees = storage.users.getAttendees(appointment, "leon", AttendeeSort.LASTNAME, Order.ASC)
        assertEquals(listOf(906), attendees.map { it.id })

        attendees = storage.users.getAttendees(appointment, "kur", AttendeeSort.LASTNAME, Order.ASC)
        assertEquals(listOf(906), attendees.map { it.id })

        attendees = storage.users.getAttendees(appointment, "leonie kurz", AttendeeSort.LASTNAME, Order.ASC)
        assertEquals(listOf(906), attendees.map { it.id })

        attendees = storage.users.getAttendees(appointment, "hein", AttendeeSort.LASTNAME, Order.ASC)
        assertEquals(listOf(), attendees.map { it.id })
    }

    @Test
    fun `get check in queue without search`() = runBlocking {
        val storage = dummy()
        val appointment = storage.appointments[901]!!
        val queue = storage.checkInQueue[appointment, "", AttendeeSort.LASTNAME, Order.ASC]

        assertEquals(listOf(904, 911, 907), queue.map { it.user })
    }

    @Test
    fun `get check in queue with search`() = runBlocking {
        val storage = dummy()
        val appointment = storage.appointments[901]!!

        var queue = storage.checkInQueue[appointment, "gust", AttendeeSort.LASTNAME, Order.ASC]
        assertEquals(listOf(904), queue.map { it.user })

        queue = storage.checkInQueue[appointment, "sim", AttendeeSort.LASTNAME, Order.ASC]
        assertEquals(listOf(911), queue.map { it.user })

        queue = storage.checkInQueue[appointment, "simon heinz", AttendeeSort.LASTNAME, Order.ASC]
        assertEquals(listOf(911), queue.map { it.user })
    }

    @Test
    fun `get enrolled not checked in without search`() = runBlocking {
        val storage = dummy()
        val appointment = storage.appointments[901]!!
        val users = storage.users.getEnrolledNotCheckedIn(appointment, "", AttendeeSort.LASTNAME, Order.ASC)

        assertEquals(listOf(903, 902), users.map { it.id })
    }

    @Test
    fun `get enrolled not checked in with search`() = runBlocking {
        val storage = dummy()
        val appointment = storage.appointments[901]!!

        var users = storage.users.getEnrolledNotCheckedIn(appointment, "emil", AttendeeSort.LASTNAME, Order.ASC)
        assertEquals(listOf(903), users.map { it.id })

        users = storage.users.getEnrolledNotCheckedIn(appointment, "peter wurst", AttendeeSort.LASTNAME, Order.ASC)
        assertEquals(listOf(902), users.map { it.id })
    }

    @Test
    fun `add pending checkin`(): Unit = runBlocking {
        val storage = dummy()

        val appointment = storage.appointments[901]!!
        val user = storage.users[903]!!

        val time = Clock.System.now()

        storage.checkInQueue.addPending(appointment, user, time)

        assertMatch(time, storage.checkInQueue[appointment, user]?.sync)
    }

    @Test
    fun `update pending checkin`(): Unit = runBlocking {
        val storage = dummy()

        val appointment = storage.appointments[901]!!
        val user = storage.users[911]!!

        val time = Clock.System.now()

        assertNotNull(storage.checkInQueue[appointment, user])

        storage.checkInQueue.addPending(appointment, user, time)

        assertMatch(time, storage.checkInQueue[appointment, user]?.sync)
    }

    @Test
    fun `add pending checkout`(): Unit = runBlocking {
        val storage = dummy()

        val appointment = storage.appointments[901]!!
        val user = storage.users[906]!!

        val time = Clock.System.now()

        storage.checkInQueue.addCheckout(appointment, user, Uuid.random(), time)

        val queue = storage.checkInQueue[appointment, user]
        assertMatch(time, queue?.sync)
        assertNotNull(queue?.attempt)
    }

    @Test
    fun `update pending checkout`(): Unit = runBlocking {
        val storage = dummy()

        val appointment = storage.appointments[901]!!
        val user = storage.users[907]!!

        val time = Clock.System.now()

        assertNotNull(storage.checkInQueue[appointment, user])

        storage.checkInQueue.addCheckout(appointment, user, Uuid.random(), time)

        assertMatch(time, storage.checkInQueue[appointment, user]?.sync)
    }

}
