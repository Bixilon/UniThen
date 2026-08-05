/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.ui.util

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.ui.AbstractComposeUiTest
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalTestApi::class)
class RememberAsyncTest : AbstractComposeUiTest() {

    @Test
    fun `value is computed`() = runComposeUiTest {
        val value = leak { rememberAsync { delay(100.milliseconds); 1 } }

        assertEquals(1, value)
    }

    @Test
    fun `value is computed with single key`() = runComposeUiTest {
        val value = leak { rememberAsync(2) { delay(100.milliseconds); 1 } }

        assertEquals(1, value)
    }

    @Test
    fun `value is computed with multiple keys`() = runComposeUiTest {
        val value = leak { rememberAsync(2, 3) { delay(100.milliseconds); 1 } }

        assertEquals(1, value)
    }
}
