package de.bixilon.unithen.api.graphql.types

import java.util.*

data class PostingQl(
    override val id: UUID,
    val product: ProductQl,
) : IdentifiedQl
