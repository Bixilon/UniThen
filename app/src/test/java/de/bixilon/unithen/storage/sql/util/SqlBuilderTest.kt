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

import de.bixilon.unithen.storage.sql.tables.CheckInQueueTable
import de.bixilon.unithen.storage.sql.util.SqlFilter.Companion.eq
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SqlBuilderTest {

    @Test
    fun `sample query`() {
        val query = SqlBuilder.select(SqlBuilder.Aggregations.Count) from "test" where (CheckInQueueTable.user eq 4) and (CheckInQueueTable.appointment eq 1)

        assertEquals(query.toSql(), SqlBuilder.SqlStatement("SELECT COUNT(*) FROM test WHERE ((checkin_queue.user=?) AND (checkin_queue.appointment=?))", listOf(4, 1)))
    }
}
