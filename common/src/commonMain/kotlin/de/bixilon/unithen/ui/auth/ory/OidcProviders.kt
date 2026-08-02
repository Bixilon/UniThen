package de.bixilon.unithen.ui.auth.ory

import unithen.common.generated.resources.Res
import unithen.common.generated.resources.auth_oidc_provider_haw_landshut
import unithen.common.generated.resources.auth_oidc_provider_hs_muenchen
import unithen.common.generated.resources.auth_oidc_provider_hs_wt
import unithen.common.generated.resources.auth_oidc_provider_lmu
import unithen.common.generated.resources.auth_oidc_provider_tum


object OidcProviders {
    const val TUM = "oidc-tum"
    const val LMU = "saml-lmu"
    const val HAW_LANDSHUT = "saml-haw-landshut"
    const val HS_MUENCHEN = "saml-hs-muenchen"
    const val HS_WT = "saml-hs-wt"

    val LOGOS = mapOf(
        TUM to "tum.svg",
        LMU to "lmu.svg",
        HAW_LANDSHUT to "haw-landshut.svg",
        HS_MUENCHEN to "hs-muenchen.svg",
        HS_WT to "hs-wt.svg",
    )

    val NAMES = mapOf(
        TUM to Res.string.auth_oidc_provider_tum,
        LMU to Res.string.auth_oidc_provider_lmu,
        HAW_LANDSHUT to Res.string.auth_oidc_provider_haw_landshut,
        HS_MUENCHEN to Res.string.auth_oidc_provider_hs_muenchen,
        HS_WT to Res.string.auth_oidc_provider_hs_wt,
    )
}
