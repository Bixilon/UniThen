package de.bixilon.unithen.api.graphql.types

import java.util.*

const val FEE_TYPE = "Fee"

data class FeeQl(
    override val id: UUID,
) : ResourceQl {
    override val __typename get() = FEE_TYPE
}
