package de.bixilon.unithen.api.graphql.types.location

const val ROOM_TYPE = "Room"

class Room(
    override val name: String,
    // TODO: floor, building
): Location {
    override val __typename get() = ROOM_TYPE
}
