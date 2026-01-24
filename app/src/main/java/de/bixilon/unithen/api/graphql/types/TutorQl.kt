package de.bixilon.unithen.api.graphql.types

import com.fasterxml.jackson.annotation.JsonProperty

data class TutorQl(
    @JsonProperty("first_name") val firstName: String,
    @JsonProperty("last_name")   val lastName: String,
)
