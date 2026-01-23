package de.bixilon.unithen.api.graphql.types.location

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
@JsonSubTypes(
    JsonSubTypes.Type(value = _Location::class, name = LOCATION_TYPE),
    JsonSubTypes.Type(value = Area::class, name = AREA_TYPE),
    JsonSubTypes.Type(value = Room::class, name = ROOM_TYPE),
)
interface Location {
    val name: String
    val __typename: String
}
