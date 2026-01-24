package de.bixilon.unithen.api.graphql.types.location

const val AREA_TYPE = "Area"

class AreaQl(
   override val name: String,
    // TODO: floor, building
) : LocationQl {
    override val __typename get() = AREA_TYPE
}
