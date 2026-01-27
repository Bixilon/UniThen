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

package de.bixilon.unithen.api.graphql.types.location

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
@JsonSubTypes(
    JsonSubTypes.Type(value = _LocationQl::class, name = LOCATION_TYPE),
    JsonSubTypes.Type(value = AreaQl::class, name = AREA_TYPE),
    JsonSubTypes.Type(value = RoomQl::class, name = ROOM_TYPE),
)
interface LocationQl {
    val name: String
    val __typename: String
}
