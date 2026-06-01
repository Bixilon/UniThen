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

package de.bixilon.unithen.storage.types

import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.unithen.storage.DbObject
import de.bixilon.unithen.storage.Key
import java.util.*
import kotlin.time.Instant

data class CheckInAttempt(
    val user: Key,
    val appointment: Key,
    val uuid: UUID?,
    val time: Instant?,
    val message: String?,
    val sync: Instant?,
    val status: Status,
) : DbObject {

    enum class Status {
        OK,
        FAILED,
        PENDING,
        ;

        companion object : ValuesEnum<Status> {
            override val VALUES = values()
            override val NAME_MAP = names()
        }
    }
}
