package de.bixilon.unithen.storage

object DefaultSites {
    const val VERSION = 1

    val SITES = listOf(
        DefaultSite("kurse.zhs-muenchen.de", "ZHS München", "zhs.svg"),
    )

    data class DefaultSite(val host: String, val name: String, val icon: String)
}
