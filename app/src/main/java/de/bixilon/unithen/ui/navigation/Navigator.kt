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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import de.bixilon.kutil.cast.CastUtil.cast
import kotlin.reflect.KClass


class Navigator(
    private val start: NavigationRoute,
    val mode: NavigationMode = NavigationMode.STANDARD,
) {
    private val stack = mutableStateListOf<Frame>()
    private val routes = HashMap<KClass<out NavigationRoute>, @Composable (NavigationRoute) -> Unit>()


    inner class Builder {
        val routes get() = this@Navigator.routes

        inline fun <reified T : NavigationRoute> composable(noinline compose: @Composable (T) -> Unit) {
            routes[T::class] = compose.cast()
        }
    }

    fun routes(builder: Builder.() -> Unit) {
        if (stack.isNotEmpty()) return

        Builder().apply(builder)
        navigate(start)
    }

    @Composable
    fun Host() {
        val visible = LocalVisibility.current
        val last = stack.last()

        for (frame in stack) {
            key(frame.route) {
                val visible = frame === last && visible
                BackHandler(visible && stack.size > 1) { pop() }


                CompositionLocalProvider(
                    LocalVisibility provides visible,
                ) {
                    Box(modifier = if (!visible) invisible else Modifier) {
                        frame.composable.invoke(frame.route)
                    }
                }
            }
        }
    }

    @Synchronized
    fun navigate(route: NavigationRoute) {
        val composable = routes[route::class] ?: throw IllegalStateException("No route registered for $route!")

        if (mode == NavigationMode.SINGLE) {
            val existing = stack.find { it.route == route }
            if (existing != null) {
                stack.removeAt(stack.indexOf(existing))
                stack += existing
                return
            }
        }

        stack += Frame(route, composable)
    }

    @Synchronized
    fun pop() {
        assert(stack.size > 1) { "Can not pop start element!" }
        stack.removeAt(stack.size - 1)
    }

    fun current() = stack.last()

    data class Frame(
        val route: NavigationRoute,
        val composable: @Composable (NavigationRoute) -> Unit,
    )

    private val invisible = Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {}
    }
}
