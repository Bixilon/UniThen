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

package de.bixilon.unithen.ui.main.checkin.scan.attendees

import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.unithen.ui.main.settings.types.Labeled
import org.jetbrains.compose.resources.StringResource
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.sort_firstname
import unithen.common.generated.resources.sort_lastname

enum class AttendeeSort(val field: String, override val label: StringResource) : Labeled {
    FIRSTNAME("firstname", Res.string.sort_firstname),
    LASTNAME("lastname", Res.string.sort_lastname),
    ;

    companion object : ValuesEnum<AttendeeSort> {
        override val VALUES = values()
        override val NAME_MAP = names()
    }
}
