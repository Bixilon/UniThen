package de.bixilon.unithen.api.graphql.types.location

const val ROOM_TYPE = "Room"

class RoomQl(
    override val name: String,
    // TODO: floor, building
) : LocationQl {
    override val __typename get() = ROOM_TYPE
}
