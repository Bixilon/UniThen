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

import de.bixilon.unithen.storage.sql.util.UserComparator.Companion.sort
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.main.checkin.scan.attendees.AttendeeSort
import de.bixilon.unithen.ui.main.checkin.scan.attendees.Order
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class UserComparatorTest {

    @Test
    fun `sort lastname with accents and special chars`() {
        val users = listOf(
            User(1, 1, Uuid.NIL, "", "Abc"),
            User(2, 1, Uuid.NIL, "", "abc"),
            User(3, 1, Uuid.NIL, "", "äbc"),
            User(4, 1, Uuid.NIL, "", "aäb"),
            User(5, 1, Uuid.NIL, "", "aaa"),
            User(6, 1, Uuid.NIL, "", "êb"),
            User(7, 1, Uuid.NIL, "", "ea"),
        )

        val sorted = users.sort(AttendeeSort.LASTNAME, Order.ASC)

        assertEquals(listOf(5, 4, 1, 2, 3, 7, 6), sorted.map { it.id })
    }

    @Test
    fun `sort lastname descending`() {
        val users = listOf(
            User(1, 1, Uuid.NIL, "A", "A"),
            User(2, 1, Uuid.NIL, "B", "C"),
            User(3, 1, Uuid.NIL, "C", "B"),
        )

        val sorted = users.sort(AttendeeSort.LASTNAME, Order.DESC)

        assertEquals(listOf(2, 3, 1), sorted.map { it.id })
    }

    @Test
    fun `sort firstname ascending`() {
        val users = listOf(
            User(1, 1, Uuid.NIL, "A", "A"),
            User(2, 1, Uuid.NIL, "C", "B"),
            User(3, 1, Uuid.NIL, "B", "C"),
        )

        val sorted = users.sort(AttendeeSort.FIRSTNAME, Order.ASC)

        assertEquals(listOf(1, 3, 2), sorted.map { it.id })
    }

    @Test
    fun `fallback to firstname if same lastname`() {
        val users = listOf(
            User(1, 1, Uuid.NIL, "A", "B"),
            User(2, 1, Uuid.NIL, "C", "A"),
            User(3, 1, Uuid.NIL, "B", "A"),
        )

        val sorted = users.sort(AttendeeSort.LASTNAME, Order.ASC)

        assertEquals(listOf(3, 2, 1), sorted.map { it.id })
    }
}
