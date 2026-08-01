package de.bixilon.unithen.ui.auth.ory


object OidcProviders {
    const val TUM = "oidc-tum"
    const val LMU = "saml-lmu"

    val LOGOS = mapOf(
        TUM to "tum.svg",
        LMU to "lmu.svg",
    )

    val NAMES = mapOf(
        TUM to "Technische Universität München",
        LMU to "Ludwig-Maximilians-Universität München",
    )
}
