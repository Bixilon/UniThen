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

import de.bixilon.unithen.storage.DbKeyed
import de.bixilon.unithen.storage.Key
import java.util.*
import kotlin.time.Instant

data class Appointment(
    override val id: Key,
    val course: Key,
    val uuid: UUID,
    val start: Instant,
    val end: Instant,
    val canceled: Instant?,
    val location: String,
) : DbKeyed
