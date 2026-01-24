package de.bixilon.unithen.api.graphql

import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.unithen.api.graphql.types.PostingQl

data class UserPkPostings(
    @field:JsonProperty("user_pk") val userPk: UserPk,
) {

    data class UserPk(
        val postings: List<PostingQl>,
    )
}
