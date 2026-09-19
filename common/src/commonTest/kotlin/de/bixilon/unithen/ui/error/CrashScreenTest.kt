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

package de.bixilon.unithen.ui.error

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.v2.runComposeUiTest
import de.bixilon.unithen.ui.AbstractComposeUiTest
import de.bixilon.unithen.ui.waitUntilText
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CrashScreenTest : AbstractComposeUiTest() {

    @Test
    fun `crash screen openes without dependencies`() = runComposeUiTest {
        setContent {
            CrashScreen("Some text", Throwable("ABC"))
        }

        waitUntilText("Some text").assertIsDisplayed()
        waitUntilText("ABC").assertIsDisplayed()
    }

    @Test
    fun `remove native trash from visible stack`() {
        val input = """
kotlin.IllegalStateException: It crashed!
    at 0   unithenios                          0x10a2cdfef        kfun:kotlin.Throwable#<init>(kotlin.String?){} + 99 
    at 1   unithenios                          0x10a2ccbcb        kfun:kotlin.Exception#<init>(kotlin.String?){} + 95 
    at 2   unithenios                          0x10a2ccd8b        kfun:kotlin.RuntimeException#<init>(kotlin.String?){} + 95 
    at 3   unithenios                          0x10a2cd293        kfun:kotlin.IllegalStateException#<init>(kotlin.String?){} + 95 
    at 4   unithenios                          0x109f053cb        kfun:de.bixilon.unithen.debug.DebugScreen$1.DebugScreen$1${'$'}invoke$${'$'}inlined${'$'}cache$4.invoke#internal + 239 (/Users/moritz/unithen/debug/src/commonMain/kotlin/de/bixilon/unithen/debug/DebugScreen.kt:59:48)
    at 13  unithenios                          0x10b1124d3        kfun:androidx.compose.ui.input.pointer.NodeParent#dispatchMainEventPass(androidx.collection.LongSparseArray<androidx.compose.ui.input.pointer.PointerInputChange>;androidx.compose.ui.layout.LayoutCoordinates;androidx.compose.ui.input.pointer.InternalPointerEvent;kotlin.Boolean){}kotlin.Boolean-impl + 515 (/opt/buildAgent/work/4fd181cf5e814865/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/input/pointer/HitPathTracker.kt:286:20)
    at 33  unithenios                          0x10a2b0ba7        kfun:kotlin.Function3#invoke(1:0;1:1;1:2){}1:3 + 115 
    at 35  unithenios                          0x10b390893        kfun:androidx.compose.ui.window.OverlayInputView.OverlayInputView$${'$'}FUNCTION_REFERENCE_FOR${'$'}handleTouchesEvent$2.invoke#internal + 227 (/opt/buildAgent/work/4fd181cf5e814865/compose/ui/ui/src/iosMain/kotlin/androidx/compose/ui/window/InputViews.ios.kt:570:26)
    at 37  unithenios                          0x10a2b0ba7        kfun:kotlin.Function3#invoke(1:0;1:1;1:2){}1:3 + 115 
    at 68  ???                                 0x104aa04e3        0x0 + 4373218531 
        """

        val expected = """k.IllegalStateException: It crashed!
at 0 k.Throwable#<init>(k.String?){} + 99
at 1 k.Exception#<init>(k.String?){} + 95
at 2 k.RuntimeException#<init>(k.String?){} + 95
at 3 k.IllegalStateException#<init>(k.String?){} + 95
at 4 d.b.u.debug.DebugScreen$1.DebugScreen$1${'$'}invoke$${'$'}inlined${'$'}cache$4.invoke#internal + 239 (/Users/moritz/unithen/debug/src/commonMain/kotlin/de/bixilon/unithen/debug/DebugScreen.kt:59:48)
at 13 a.x.ui.input.pointer.NodeParent#dispatchMainEventPass(androidx.collection.LongSparseArray<a.x.ui.input.pointer.PointerInputChange>;a.x.ui.layout.LayoutCoordinates;a.x.ui.input.pointer.InternalPointerEvent;k.Boolean){}k.Boolean-impl + 515 (/opt/buildAgent/work/4fd181cf5e814865/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/input/pointer/HitPathTracker.kt:286:20)
at 33 k.Function3#invoke(1:0;1:1;1:2){}1:3 + 115
at 35 a.x.ui.window.OverlayInputView.OverlayInputView$${'$'}FUNCTION_REFERENCE_FOR${'$'}handleTouchesEvent$2.invoke#internal + 227 (/opt/buildAgent/work/4fd181cf5e814865/compose/ui/ui/src/iosMain/kotlin/androidx/compose/ui/window/InputViews.ios.kt:570:26)
at 37 k.Function3#invoke(1:0;1:1;1:2){}1:3 + 115
at 68 ??? 0x0 + 4373218531"""

        val formatted = input.formatDisplay()
        assertEquals(expected, formatted)
    }
}
