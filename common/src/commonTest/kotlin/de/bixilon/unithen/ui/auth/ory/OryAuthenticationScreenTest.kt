package de.bixilon.unithen.ui.auth.ory

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class OryAuthenticationScreenTest : AbstractComposeUiTest() {

    @Test
    fun `display card correctly with name`() = runComposeUiTest {
        setContent { FlowRow { OidcCard(OryConfig.OryOidc("oidc-tum", "oidc-tum", null)) {} } }

        waitUntilText("Technische").assertIsDisplayed()
    }
}
