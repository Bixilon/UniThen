package de.bixilon.unithen.api.graphql.types

import java.util.*

data class Posting(
    override val id: UUID,
    val product: Product,
) : Identified
