package de.bixilon.unithen.api

import com.fasterxml.jackson.module.kotlin.readValue
import de.bixilon.unithen.api.graphql.UserPkPostings
import de.bixilon.unithen.api.graphql.http.GrapQlResponse
import de.bixilon.unithen.api.graphql.http.GraphQlRequest
import de.bixilon.unithen.api.graphql.query.QlQuery
import de.bixilon.unithen.api.graphql.query.QueryLoader
import de.bixilon.unithen.api.graphql.types.PostingQl
import de.bixilon.unithen.util.Jackson
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.util.*

open class UniNowApi(
    val url: URI,
) {

    protected open fun buildRequest(endpoint: String) = HttpUtil.create(url, endpoint)

    fun postJson(endpoint: String, payload: Any?): String {
        val body = payload?.let { Jackson.MAPPER.writeValueAsBytes(it).toRequestBody(HttpUtil.JSON) } ?: RequestBody.EMPTY
        val request = buildRequest(endpoint)
            .post(body)
            .build()


        val client = OkHttpClient().newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        val response = client.newCall(request).execute()

        if (response.code != 200) throw IllegalStateException("Request is not OK: ${response.code}: ${response.body.string()}")

        return response.body.string()
    }

    inline fun <reified T> graphql(name: String, vararg variables: Pair<String, Any>) = graphql<T>(QueryLoader[name], *variables)
    inline fun <reified T> graphql(query: QlQuery, vararg variables: Pair<String, Any>): T {
        val request = GraphQlRequest(query.query, variables.toMap())
        val response = postJson("/api/query", request)

        return Jackson.GRAPH_QL.readValue<GrapQlResponse<T>>(response).data
    }

    fun postings(userId: UUID): List<PostingQl> {
        return graphql<UserPkPostings>("courses", "userID" to userId).userPk.postings
    }
}
