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

package de.bixilon.unithen.storage

import de.bixilon.kutil.uuid.UuidUtil.toUuid
import de.bixilon.unithen.api.graphql.types.AppointmentQl
import de.bixilon.unithen.api.graphql.types.CourseQl
import de.bixilon.unithen.api.graphql.types.EventQl
import de.bixilon.unithen.api.graphql.types.location.RoomQl
import de.bixilon.unithen.api.graphql.types.user.CourseUserQl
import de.bixilon.unithen.storage.StorageTestUtil.course
import de.bixilon.unithen.storage.StorageTestUtil.site
import de.bixilon.unithen.storage.StorageUtil.storeCourse
import de.bixilon.unithen.storage.StorageUtil.storeEnrolled
import de.bixilon.unithen.storage.sql.empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class StorageUtilTest {
    private val A = "00000000-0000-0000-0000-000000000000".toUuid()
    private val B = "10000000-0000-0000-0000-000000000000".toUuid()
    private val C = "20000000-0000-0000-0000-000000000000".toUuid()
    private val D = "30000000-0000-0000-0000-000000000000".toUuid()
    private val E = "40000000-0000-0000-0000-000000000000".toUuid()
    private val F = "50000000-0000-0000-0000-000000000000".toUuid()
    private val G = "60000000-0000-0000-0000-000000000000".toUuid()
    private val H = "70000000-0000-0000-0000-000000000000".toUuid()


    init {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `store new course without tutor data`() = runBlocking {
        val storage = empty()
        val site = storage.site()

        val courseQl = CourseQl(
            A, "course",
            EventQl(B, "event", Clock.System.now(), Clock.System.now()),
            listOf(
                CourseUserQl(C, "a", "b"),
                CourseUserQl(D, "b", "c"),
            ),
            listOf(
                AppointmentQl(E, Clock.System.now(), Clock.System.now(), null, listOf(), RoomQl("room"))
            ),
        )

        storage.storeCourse(site, courseQl)

        assertEquals("course", storage.courses[site, A]!!.name)

        assertEquals("event", storage.events[site, B]!!.name)

        assertEquals("a", storage.users[site, C]!!.firstname)
        assertEquals("b", storage.users[site, D]!!.firstname)

        assertEquals("room", storage.appointments[E].first().location)
    }

    @Test
    fun `store enrolled`() = runBlocking {
        val storage = empty()

        val course = storage.course()
        val site = storage.sites[course.site]!!

        storage.storeEnrolled(site, course, listOf(
            CourseUserQl(A, "a", "b"),
            CourseUserQl(B, "b", "c"),
        ))

        val enrolled = storage.users.getEnrolled(course)

        assertEquals(2, enrolled.size)
        assertEquals(A, enrolled.find { it.firstname == "a" }!!.uuid)
    }

    @Test
    fun `remove unenrolled users`() = runBlocking {
        val storage = empty()

        val course = storage.course()
        val site = storage.sites[course.site]!!

        storage.storeEnrolled(site, course, listOf(
            CourseUserQl(A, "a", "b"),
            CourseUserQl(B, "b", "c"),
        ))
        storage.storeEnrolled(site, course, listOf(
            CourseUserQl(C, "c", "d"),
        ))

        val enrolled = storage.users.getEnrolled(course)

        assertEquals(1, enrolled.size)
        assertEquals(C, enrolled.find { it.firstname == "c" }!!.uuid)
    }
}
