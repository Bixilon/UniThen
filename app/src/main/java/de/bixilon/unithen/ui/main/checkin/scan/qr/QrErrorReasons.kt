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

package de.bixilon.unithen.ui.main.checkin.scan.qr

import de.bixilon.unithen.ui.main.settings.types.Labeled

enum class QrErrorReasons(override val label: String) : Labeled {
    INVALID_FORMAT("Invalid QR code format!"),
    INVALID_DATA("Invalid QR code data!"),

    INVALID_APPOINTMENT("Invalid appointment (wrong course?)!"),
    UNKNOWN_USER("Unknown user!"),
    NOT_ENROLLED("User not enrolled in course!"),
    ALREADY_CHECKED_IN("User is already checked in"),


    OTHER("Unknown error")
}
