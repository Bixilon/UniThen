/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.api

import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.api.graphql.http.GraphQlException
import de.bixilon.unithen.api.graphql.http.GraphQlRequest
import de.bixilon.unithen.api.graphql.http.GraphQlResponse
import de.bixilon.unithen.api.graphql.query.QlQuery
import de.bixilon.unithen.api.graphql.query.QueryLoader
import de.bixilon.unithen.ui.error.SerializationCrash
import de.bixilon.unithen.util.Jackson
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.util.concurrent.TimeUnit

open class UniNowApi(
    val url: URI,
) {

    protected open fun buildRequest(endpoint: String) = HttpUtil.create(url, endpoint)

    inline fun <reified I> postJson(endpoint: String, payload: I): String {
        val payload = Jackson.MAPPER.encodeToString(payload).toRequestBody(HttpUtil.JSON)
        return postJson(endpoint, payload)
    }

    fun postJson(endpoint: String, payload: RequestBody = RequestBody.EMPTY): String {
        val request = buildRequest(endpoint)
            .post(payload)
            .build()


        val client = OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        val response = client.newCall(request).execute()

        if (response.code != 200) throw IllegalStateException("Request is not OK: ${response.code}: ${response.body.string()}")

        return response.body.string()
    }

    inline fun <reified T> graphql(name: String, vararg variables: Pair<String, JsonElement>) = graphql<T>(QueryLoader[name], *variables)
    inline fun <reified T> graphql(query: QlQuery, vararg variables: Pair<String, JsonElement>): T {
        val request = GraphQlRequest(query.query, JsonObject(variables.toMap()))

        val response = postJson("/api/query", request)

        val graphql = try {
            Jackson.GRAPHQL.decodeFromString<GraphQlResponse<T>>(response)
        }catch (error: SerializationException) {
            throw SerializationCrash(response, error)
        }

        if (graphql.errors != null && graphql.errors.isNotEmpty()) {
            if (graphql.errors.size == 1 && graphql.errors.first().message == "unauthenticated") {
                throw AuthenticationException("GraphQl: Not authenticated!")
            }

            throw GraphQlException(graphql.errors)
        }

        return graphql.data
    }
}
