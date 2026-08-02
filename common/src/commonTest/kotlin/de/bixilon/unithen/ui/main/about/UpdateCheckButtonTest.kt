package de.bixilon.unithen.ui.main.about

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.waitUntil
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class UpdateCheckButtonTest : AbstractComposeUiTest() {

    @Test
    fun `idle when not clicked on it`() = runComposeUiTest {
        setContent { UpdateCheckButton() }

        waitUntilText("Check for updates", substring = false).assertIsDisplayed()
    }

    @Test
    fun `checking when clicking on it`() = runComposeUiTest {
        setContent { UpdateCheckButton() }

        waitUntil(hasClickAction()).performClick()

        waitUntilText("Checking for updates").assertIsDisplayed()
    }
}
