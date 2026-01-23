package de.bixilon.unithen.api.graphql.types.location

const val LOCATION_TYPE = "Location"

class _Location(
   override val name: String,
    // TODO: floor, building
): Location {
    override val __typename get() = LOCATION_TYPE
}
