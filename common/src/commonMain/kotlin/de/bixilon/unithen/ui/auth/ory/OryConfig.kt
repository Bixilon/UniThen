package de.bixilon.unithen.ui.auth.ory

import de.bixilon.unithen.api.ory.OryAuthenticationToken
import de.bixilon.unithen.api.ory.OryOidcResponse
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
        TODO()
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
