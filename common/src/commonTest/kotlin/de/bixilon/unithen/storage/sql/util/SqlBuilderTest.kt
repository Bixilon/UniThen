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

import de.bixilon.unithen.storage.sql.tables.AccountCourses
import de.bixilon.unithen.storage.sql.tables.AppointmentTable
import de.bixilon.unithen.storage.sql.tables.CheckInQueueTable
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.eq
import kotlin.test.Test
import kotlin.test.assertEquals

class SqlBuilderTest {

    @Test
    fun `sample query`() {
        val query = SqlBuilder.select(SqlBuilder.Aggregations.Count) from "test" where (CheckInQueueTable.user eq 4) and (CheckInQueueTable.appointment eq 1)

        assertEquals(query.toSql(), SqlBuilder.SqlStatement("SELECT COUNT(*) FROM test WHERE ((checkin_queue.user=?) AND (checkin_queue.appointment=?))", listOf(4, 1)))
    }

    @Test
    fun `insert with one value row`() {
        val query = SqlBuilder.insert(AppointmentTable, AppointmentTable.location to "hello", AppointmentTable.course to 3)

        assertEquals(query.toSql(), SqlBuilder.SqlStatement("INSERT INTO appointments(location,course) VALUES(?,?)", listOf("hello", 3)))
    }

    @Test
    fun `select 1 query`() {
        val query = SqlBuilder.select("1").from(AccountCourses)
            .where(AccountCourses.tutor eq true)

        assertEquals(query.toSql(), SqlBuilder.SqlStatement("SELECT 1 FROM account_courses WHERE (account_courses.tutor=?)", listOf(true)))
    }

    @Test
    fun `query exists query`() {
        val query = SqlFilter.exists(SqlBuilder.select("1").from(AccountCourses)
            .where(AccountCourses.tutor eq true))

        assertEquals("EXISTS (SELECT 1 FROM account_courses WHERE (account_courses.tutor=?))", query.sql)
        assertEquals(listOf(true), query.parameters)
    }
}
