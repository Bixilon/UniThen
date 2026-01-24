package de.bixilon.unithen.api.graphql.types

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
@JsonSubTypes(
    JsonSubTypes.Type(value = Course::class, name = COURSE_TYPE),
    JsonSubTypes.Type(value = Fee::class, name = FEE_TYPE),
)
interface Resource : Identified {
    val __typename: String
}
