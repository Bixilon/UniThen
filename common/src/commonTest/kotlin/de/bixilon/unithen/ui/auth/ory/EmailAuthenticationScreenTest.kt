package de.bixilon.unithen.ui.auth.ory

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.storage.types.Site
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test
import kotlin.time.Instant
import kotlin.uuid.Uuid

@OptIn(ExperimentalTestApi::class)
class EmailAuthenticationScreenTest : AbstractComposeUiTest() {

    @Test
    fun `login button is enabled when entering correct details`() = runComposeUiTest {
        setContent { EmailAuthenticationScreen(Site(1, "test.de", "na", null, Instant.DISTANT_PAST), OryConfig(Uuid.random(), "test", listOf())) }

        waitUntilText("Email").onParent().performTextInput("test@test.de")
        waitUntilText("Password").onParent().performTextInput("secret")

        waitUntilText("Login").assertIsEnabled()
    }
}
