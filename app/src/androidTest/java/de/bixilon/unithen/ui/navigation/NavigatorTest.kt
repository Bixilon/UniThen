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

import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test


object NoDateRoute : NavigationRoute
class RouteData(val text: String) : NavigationRoute
object StateRoute : NavigationRoute

@Composable
fun NoDataScreen() {
    Text("hello A")
}

@Composable
fun DataScreen(text: String) {
    Text("hello $text")
}

@Composable
fun StateScreen() {
    var state by remember { mutableStateOf(false) }
    Text("clicked: $state")
    Checkbox(state, { state = it })
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

class NavigatorTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun test_setup() {
        rule.setContent { NoDataScreen() }

        rule.onNodeWithText("hello A").assertIsDisplayed()
    }

    @Test
    fun correct_home_text() {
        rule.setContent { TestNavigator {} }

        rule.onNodeWithText("hello A").assertIsDisplayed()
    }

    @Test
    fun correct_navigation_to_b() {
        rule.setContent { TestNavigator { it.navigate(RouteData("yes")) } }

        rule.onNodeWithText("hello A").assertIsNotDisplayed()
        rule.onNodeWithText("hello yes").assertIsDisplayed()
    }

    @Test
    fun correct_popping() {
        var navigator: Navigator? = null
        rule.setContent { TestNavigator { navigator = it } }

        navigator!!.navigate(RouteData("yes"))
        navigator!!.pop()

        rule.onNodeWithText("hello A").assertIsDisplayed()
        rule.onNodeWithText("hello yes").assertIsNotDisplayed()
    }

    @Test
    fun state_preserved_when_getting_back() {
        var navigator: Navigator? = null
        rule.setContent { TestNavigator { navigator = it } }
        navigator!!.navigate(StateRoute)

        rule.onNode(isEditable()).performClick()
        rule.onNodeWithText("clicked true").assertIsDisplayed()

        navigator.navigate(NoDateRoute)
        navigator.pop()

        rule.onNodeWithText("clicked true").assertIsDisplayed()
    }
}
