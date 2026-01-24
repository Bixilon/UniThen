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
