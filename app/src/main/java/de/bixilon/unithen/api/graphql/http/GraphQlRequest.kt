package de.bixilon.unithen.api.graphql.http

data class GraphQlRequest(
    val query: String,
    val variables: Map<String, Any>,
)
