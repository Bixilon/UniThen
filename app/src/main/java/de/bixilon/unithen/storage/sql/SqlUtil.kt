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

import android.database.Cursor
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import kotlin.time.Instant

object SqlUtil {

    fun Cursor.getUUID(index: Int) = getString(index).toUUID()
    fun Cursor.getInstant(index: Int) = Instant.fromEpochSeconds(getLong(index), 0)

    fun Any?.db(): String? = when (this) {
        null -> null
        is Instant -> epochSeconds.toString()
        else -> this.toString()
    }
}
