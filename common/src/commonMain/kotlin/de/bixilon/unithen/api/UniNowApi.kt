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
import de.bixilon.unithen.http.CLIENT
import de.bixilon.unithen.ui.error.SerializationExceptionData
import de.bixilon.unithen.util.Jackson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

open class UniNowApi(
    val host: String,
) {

    protected open suspend fun buildRequest(endpoint: String) = HttpUtil.create(host, endpoint)

    suspend inline fun <reified I> postJson(endpoint: String, payload: I): String {
        val payload = Jackson.MAPPER.encodeToString(payload)
        return postJson(endpoint, payload)
    }

    suspend fun get(endpoint: String): String {
        val request = buildRequest(endpoint).apply { method = HttpMethod.Get }

        val response = CLIENT.get(request)

        if (response.status != HttpStatusCode.OK) throw IllegalStateException("Request is not OK: ${response.status}: ${response.bodyAsText()}")

        return response.bodyAsText()
    }

    suspend fun postJson(endpoint: String, payload: String): String {
        val request = buildRequest(endpoint).apply { method = HttpMethod.Post; setBody(payload) }


        val response = CLIENT.post(request)

        if (response.status != HttpStatusCode.OK) throw IllegalStateException("Request is not OK: ${response.status}: ${response.bodyAsText()}")

        return response.bodyAsText()
    }

    suspend inline fun <reified T> graphql(name: String, vararg variables: Pair<String, JsonElement>) = graphql<T>(QueryLoader[name], *variables)
    suspend inline fun <reified T> graphql(query: QlQuery, vararg variables: Pair<String, JsonElement>): T {
        val request = GraphQlRequest(query.query, JsonObject(variables.toMap()))

        val response = postJson("/api/query", request)

        val graphql = try {
            Jackson.GRAPHQL.decodeFromString<GraphQlResponse<T>>(response)
        } catch (error: SerializationException) {
            throw SerializationExceptionData(response, error)
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
