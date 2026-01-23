package de.bixilon.unithen.api.graphql.types

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class Product(
    override val id: UUID,
    @JsonProperty("needs_approval") val needsApproval: Boolean,
    val resource: Resource,
): Identified
