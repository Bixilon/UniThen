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

import androidx.compose.ui.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.waitUntil(matcher: SemanticsMatcher, timeout: Duration = 1.seconds): SemanticsNodeInteraction {
    waitUntilAtLeastOneExists(matcher, timeout.inWholeMilliseconds)
    return onNode(matcher)
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.waitUntilText(text: String, timeout: Duration = 1.seconds, substring: Boolean = true, matcher: SemanticsMatcher? = null): SemanticsNodeInteraction {
    return waitUntil(hasText(text, substring = substring).let { matcher?.and(it) ?: it }, timeout)
}
