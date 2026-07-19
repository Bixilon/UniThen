package de.bixilon.unithen.ui.auth.ory

import androidx.compose.ui.graphics.vector.ImageVector

object OidcProviders {
    const val TUM = "oidc-tum"
    const val LMU = "saml-lmu"

    val LOGOS = mapOf<String, ImageVector>(
        // TUM to LogoTUM,
        // LMU to LogoLMU,
    )

    val NAMES = mapOf(
        TUM to "Technische Universität München",
        LMU to "Ludwig-Maximilians-Universität München",
    )
}
