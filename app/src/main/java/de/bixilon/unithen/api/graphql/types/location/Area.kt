package de.bixilon.unithen.api.graphql.types.location

const val AREA_TYPE = "Area"

class Area(
   override val name: String,
    // TODO: floor, building
): Location {
    override val __typename get() = AREA_TYPE
}
