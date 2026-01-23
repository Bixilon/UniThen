package de.bixilon.unithen.storage

import java.net.URI

data class Site(
    override val id: Int,
    val url: URI,
) : DbKeyed
