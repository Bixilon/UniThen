package de.bixilon.unithen.ui.auth.ory

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
}
