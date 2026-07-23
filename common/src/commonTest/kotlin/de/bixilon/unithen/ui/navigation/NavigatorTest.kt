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

package de.bixilon.unithen.ui.navigation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test


object NoDateRoute : NavigationRoute
class RouteData(val text: String) : NavigationRoute
object StateRoute : NavigationRoute

@Composable
fun NoDataScreen() {
    Text("nothing")
}

@Composable
fun DataScreen(text: String) {
    Text("data: $text")
}

@Composable
fun StateScreen() {
    var clicks by remember { mutableStateOf(0) }
    Text("clicked: $clicks")
    Button({ clicks++ }) { Text("Click me!") }
}

@Composable
fun TestNavigator(effect: (Navigator) -> Unit) {
    val navigator = remember { Navigator(NoDateRoute) }


    navigator.routes {
        composable<NoDateRoute> { NoDataScreen() }
        composable<RouteData> { DataScreen(it.text) }
        composable<StateRoute> { StateScreen() }
    }

    navigator.Host()

    LaunchedEffect(Unit) { effect.invoke(navigator) }
}

@OptIn(ExperimentalTestApi::class)
class NavigatorTest {

    @Test
    fun `test basic setup`() = runComposeUiTest {
        setContent {
            setContent { NoDataScreen() }
        }

        onNodeWithText("nothing").assertIsDisplayed()
    }

    @Test
    fun `initial home`() = runComposeUiTest {
        setContent { TestNavigator {} }

        onNodeWithText("nothing").assertIsDisplayed()
    }

    @Test
    fun `navigation to b`() = runComposeUiTest {
        setContent { TestNavigator { it.navigate(RouteData("yes")) } }

        onNodeWithText("nothing").assertIsNotDisplayed()
        onNodeWithText("data: yes").assertIsDisplayed()
    }

    @Test
    fun `popping of screen`() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent { TestNavigator { navigator = it } }

        navigator!!.navigate(RouteData("yes"))
        navigator.pop()

        waitUntilDoesNotExist(hasText("data: yes"))

        onNodeWithText("nothing").assertIsDisplayed()
    }

    @Test
    fun `state preserved when popping`() = runComposeUiTest {
        var navigator: Navigator? = null
        setContent { TestNavigator { navigator = it } }
        navigator!!.navigate(StateRoute)


        waitUntilAtLeastOneExists(hasText("Click me!"))
        onNode(hasText("Click me!")).performClick()
        onNodeWithText("clicked: 1").assertIsDisplayed()

        navigator.navigate(NoDateRoute)
        navigator.pop()

        onNodeWithText("clicked: 1").assertIsDisplayed()
    }
}
