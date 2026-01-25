package de.bixilon.unithen.storage

import java.net.URI
import kotlin.time.Instant

data class Site(
    override val id: Int,
    val url: URI,

    val name: String,
    val icon: ByteArray,

    val fetched: Instant,
) : DbKeyed
