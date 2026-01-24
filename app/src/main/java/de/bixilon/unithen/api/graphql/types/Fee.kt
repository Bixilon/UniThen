package de.bixilon.unithen.api.graphql.types

import java.util.*

const val FEE_TYPE = "Fee"

data class Fee(
    override val id: UUID,
) : Resource {
    override val __typename get() = FEE_TYPE
}
