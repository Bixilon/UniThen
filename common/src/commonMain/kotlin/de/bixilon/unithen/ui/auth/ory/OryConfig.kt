package de.bixilon.unithen.ui.auth.ory

import de.bixilon.unithen.api.HttpUtil
import de.bixilon.unithen.api.ory.OryAuthenticationToken
import de.bixilon.unithen.api.ory.OryOidcResponse
import de.bixilon.unithen.http.CLIENT
import de.bixilon.unithen.util.Jackson
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
        val data = mapOf(
            "identifier" to email,
            "password" to password,
            "method" to "password"
        )
        TODO()
    }
}
