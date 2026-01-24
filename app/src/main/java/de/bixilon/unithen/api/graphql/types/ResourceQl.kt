package de.bixilon.unithen.api.graphql.types

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "__typename")
@JsonSubTypes(
    JsonSubTypes.Type(value = CourseQl::class, name = COURSE_TYPE),
    JsonSubTypes.Type(value = FeeQl::class, name = FEE_TYPE),
)
interface ResourceQl : IdentifiedQl {
    val __typename: String
}
