package de.bixilon.unithen.api.graphql.types

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ProductQl(
    override val id: UUID,
    @JsonProperty("needs_approval") val needsApproval: Boolean,
    val resource: ResourceQl,
) : IdentifiedQl
