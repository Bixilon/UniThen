package de.bixilon.unithen.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test


object RouteA : NavigationRoute
class RouteB(val text: String) : NavigationRoute

@Composable
fun ScreenA() {
    Text("hello A")
}

@Composable
fun ScreenB(text: String) {
    Text("hello $text")
}

@Composable
fun TestNavigator(effect: (Navigator) -> Unit) {
    val navigator = remember { Navigator(RouteA) }


    navigator.routes {
        composable<RouteA> { ScreenA() }

        composable<RouteB> { ScreenB(it.text) }
    }

    navigator.Host()

    LaunchedEffect(Unit) { effect.invoke(navigator) }
}

class NavigatorTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun test_setup() {
        composeTestRule.setContent { ScreenA() }

        composeTestRule.onNodeWithText("hello A").assertIsDisplayed()
    }

    @Test
    fun correct_home_text() {
        composeTestRule.setContent { TestNavigator {} }

        composeTestRule.onNodeWithText("hello A").assertIsDisplayed()
    }

    @Test
    fun correct_navigation_to_b() {
        composeTestRule.setContent { TestNavigator { it.navigate(RouteB("yes")) } }

        composeTestRule.onNodeWithText("hello yes").assertIsDisplayed()
    }
}
