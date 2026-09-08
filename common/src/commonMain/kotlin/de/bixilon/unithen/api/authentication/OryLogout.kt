package de.bixilon.unithen.api.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OryLogout(
    @SerialName("session_token") val sessionToken: String,
)
