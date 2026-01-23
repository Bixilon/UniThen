package de.bixilon.unithen.api

import android.net.http.HttpEngine
import com.fasterxml.jackson.module.kotlin.readValue
import de.bixilon.unithen.api.authentication.Authentication
import de.bixilon.unithen.api.graphql.http.GrapQlResponse
import de.bixilon.unithen.api.graphql.http.GraphQlRequest
import de.bixilon.unithen.api.graphql.query.QlQuery
import de.bixilon.unithen.api.graphql.query.QueryLoader
import de.bixilon.unithen.util.Jackson
import java.io.InputStream
import java.net.URL

open class UniNowApi(
    val url: URL,
) {

    protected open fun buildRequest(endpoint: String) {
        TODO()
    }

    fun postJson(endpoint: String, payload: Any): InputStream {
        /*
        val request = ENGINE.newUrlRequestBuilder(url.toString() + endpoint)
            .build()


        authentication?.authenticate(request)
         */

        TODO()
    }

    inline fun <reified T> graphql(name: String, vararg variables: Pair<String, Any>) = graphql<T>(QueryLoader[name], *variables)
    inline fun <reified T> graphql(query: QlQuery, vararg variables: Pair<String, Any>): T {
        val request = GraphQlRequest(query.query, variables.toMap())
        val response = postJson("/api/query", request)

        return Jackson.GRAPH_QL.readValue<GrapQlResponse<T>>(response).data
    }
}
