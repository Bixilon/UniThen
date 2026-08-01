package de.bixilon.unithen.ui.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.api.authentication.CookieAuthentication
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LegacyWebViewAuthTest {

    @Test
    fun `setup is working`() = runComposeUiTest {
        var authentication: CookieAuthentication? = null
        setContent { LegacyWebviewAuthentication("bixilon.de") { authentication = it } }

        waitForIdle()

        assertEquals(null, authentication)
    }
}
