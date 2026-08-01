package de.bixilon.unithen.ui.auth.ory

import kotlinx.coroutines.runBlocking
import unithen.common.generated.resources.Res
import kotlin.test.Test
import kotlin.test.assertTrue

class OidcProviderTest {

    @Test
    fun `all logos exist`() {
        for ((_, file) in OidcProviders.LOGOS) {
            val data = runBlocking { Res.readBytes("files/logo/$file") }
            assertTrue { data.isNotEmpty() }
        }
    }
}
