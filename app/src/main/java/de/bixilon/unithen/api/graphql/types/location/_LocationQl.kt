package de.bixilon.unithen.api.graphql.types.location

const val LOCATION_TYPE = "Location"

class _LocationQl(
   override val name: String,
    // TODO: floor, building
) : LocationQl {
    override val __typename get() = LOCATION_TYPE
}
