package de.bixilon.unithen.ui.auth.ory

import de.bixilon.unithen.api.HttpUtil
import de.bixilon.unithen.api.ory.OryAuthenticationToken
import de.bixilon.unithen.api.ory.OryOidcResponse
import de.bixilon.unithen.http.CLIENT
import de.bixilon.unithen.util.Jackson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlin.uuid.Uuid

data class OryConfig(
    val id: Uuid,
    val url: String,
    val oidc: List<OryOidc>,
) {

    data class OryOidc(
        val id: String,
        val value: String,
        val name: String?,
    )


    suspend fun loginOidc(oidc: OryOidc): OryOidcResponse {
        val request = HttpUtil.create(url).apply {
            method = HttpMethod.Post
            parameter("provider", oidc.value)
            accept(ContentType.Application.Json)
        }
        val response = CLIENT.get(request)

        if (response.status != HttpStatusCode.UnprocessableEntity) throw IllegalStateException("422 expected: ${response.status}: ${response.bodyAsText()}")

        return Jackson.MAPPER.decodeFromString(response.bodyAsText())
    }

    suspend fun loginEmail(email: String, password: String): OryAuthenticationToken {
        val request = HttpUtil.create(url).apply {
            method = HttpMethod.Post

            contentType(ContentType.Application.Json)
            setBody(JsonObject(mapOf(
                "identifier" to JsonPrimitive(email),
                "password" to JsonPrimitive(password),
                "method" to JsonPrimitive("password"),
            )).toString())

            accept(ContentType.Application.Json)
        }
        val response = CLIENT.post(request)

        if (response.status == HttpStatusCode.BadRequest) {
            val json = Jackson.MAPPER.decodeFromString<JsonObject>(response.bodyAsText())

            val message = json.jsonObject["ui"]?.jsonObject?.get("messages")?.jsonArray?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            throw InvalidCredentialException(message ?: "Bad request?")
        }

        if (response.status != HttpStatusCode.OK) throw IllegalStateException("Request is not ok: ${response.status}: ${response.bodyAsText()}")

        return Jackson.MAPPER.decodeFromString(response.bodyAsText())
    }
}
