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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class Appointment(
    override val id: Key,
    val course: Key,
    val uuid: Uuid,
    val start: Instant,
    val end: Instant,
    val canceled: Instant?,
    val location: String,

    val fetchedAttendees: Instant?,
) : DbKeyed {

    fun isAttendeesStale(now: Instant = Clock.System.now()) = fetchedAttendees == null || now - fetchedAttendees > ATTENDEES_CACHE_TTL

    fun canPerformCheckIn(now: Instant = Clock.System.now()) = now in (start - CHECKIN_EARLY_DURATION..end + CHECKIN_LATE_DURATION)
    fun canSyncCheckIn(now: Instant = Clock.System.now()) = now in (start - CHECKIN_EARLY_DURATION..end + CHECKIN_LATE_SYNC_DURATION)

    companion object {
        // TODO: The api provides both values, but they seem to be configurable and vary from course to course. That is safe default (otherwise "checkin closed") error is thrown, whatever...
        val CHECKIN_EARLY_DURATION = 1.hours + 30.minutes
        val CHECKIN_LATE_DURATION = 15.minutes

        val CHECKIN_LATE_SYNC_DURATION = 1.hours

        val ATTENDEES_CACHE_TTL = 1.hours
    }
}
