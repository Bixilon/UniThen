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

package de.bixilon.unithen.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.RuntimeInfo.RuntimeInfo0
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.storage.sql.TestSqlHelper
import de.bixilon.unithen.ui.storage.LocalStorage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalTestApi::class)
class LoaderTest {
    init {
        RuntimeInfo0.actual = object : RuntimeInfo {
            override val debug get() = true
        }
    }

    @Composable
    private fun TestLoader(helper: TestSqlHelper = TestSqlHelper()) {
        CompositionLocalProvider(
            LocalStorage.provides(SqlStorage(helper)),
        ) {
            Loader { Text("Content") }
        }
    }

    // @Test
    fun `initial view while loading`() = runComposeUiTest {
        setContent {
                TestLoader(object : TestSqlHelper() {
                    override suspend fun load() {
                        delay(100.seconds)
                    }
                })
        }

        waitUntilText("Content")

        onNodeWithContentDescription("logo").assertIsDisplayed()
    }

    // @Test
    fun `crash while loading`() = runComposeUiTest {
        setContent {
                TestLoader(object : TestSqlHelper() {
                    override suspend fun load() {
                        throw IllegalStateException("Expected crash")
                    }
                })
        }

        waitUntilText("Expected crash").assertIsDisplayed()
    }

    // @Test
    fun `load content after 10ms`() = runComposeUiTest {
        setContent {
                TestLoader(object : TestSqlHelper() {
                    override suspend fun load() {
                        delay(10.seconds)
                    }
                })
        }

        waitUntilText("Content").assertIsDisplayed()
    }
}
