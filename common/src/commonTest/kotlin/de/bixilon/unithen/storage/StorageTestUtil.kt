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

import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.user.UserDetails
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.types.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

object StorageTestUtil {


    fun SqlStorage.site(name: String = "Test site", host: String = "site.test"): Site {
        return sites.add(name, host, null)
    }

    fun SqlStorage.account(site: Site = site(), uuid: Uuid = Uuid.random(), firstname: String = "Firstname", lastname: String = "Lastname", sessionKey: String = "a"): Account {
        return accounts.add(site, UserDetails(uuid, firstname, lastname), CookieAuthentication(sessionKey))
    }

    fun SqlStorage.event(site: Site = site(), uuid: Uuid = Uuid.random(), name: String = "Test event", start: Instant = Clock.System.now(), end: Instant = Clock.System.now()): Event {
        return events.add(site, uuid, name, start, end)
    }

    fun SqlStorage.course(event: Event = event(), uuid: Uuid = Uuid.random(), name: String = "Test course", fetched: Instant = Clock.System.now()): Course {
        return courses.add(sites[event.site]!!, event, uuid, name, fetched)
    }

    fun SqlStorage.appointment(course: Course = course(), uuid: Uuid = Uuid.random(), start: Instant = Clock.System.now(), end: Instant = Clock.System.now(), canceled: Instant? = null): Appointment {
        return appointments.add(course, uuid, start, end, canceled, "")
    }
}
