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

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import de.bixilon.kutil.cast.CastUtil.cast
import kotlin.reflect.KClass


class Navigator(
    private val start: NavigationRoute,
) {
    private val stack = mutableStateListOf<Frame>()
    private val routes = HashMap<KClass<out NavigationRoute>, @Composable (NavigationRoute) -> Unit>()
    private var isNavigating by mutableStateOf(false)


    inner class Builder {
        val routes get() = this@Navigator.routes

        inline fun <reified T : NavigationRoute> composable(noinline compose: @Composable (T) -> Unit) {
            routes[T::class] = compose.cast()
        }
    }

    fun Context.findActivity(): ComponentActivity? = when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    fun routes(builder: Builder.() -> Unit) {
        if (stack.isNotEmpty()) return

        Builder().apply(builder)
        navigate(start)
    }

    @Composable
    fun Host() {
        val context = LocalContext.current
        BackHandler { if (stack.size > 1) pop() else context.findActivity()?.finish() }

        val frame = stack.last()

        var previousStackSize by remember { mutableIntStateOf(stack.size) }
        val isForward = stack.size > previousStackSize

        LaunchedEffect(stack.size) {
            previousStackSize = stack.size
            isNavigating = false
        }

        CompositionLocalProvider(
            LocalNavigation provides this,
        ) {
            val target = frame
            if (frame.content == null) {
                frame.content = movableContentOf { target.composable(target.route) }
                }
            frame.content!!.invoke()
        }
    }

    fun navigate(route: NavigationRoute) {
        val composable = routes[route::class] ?: throw IllegalStateException("No route registered for $route!")

        if (isNavigating) {
            Log.w("NAV", "Ignoring route $route (already navigating)")
            return
        }

        isNavigating = true
        stack += Frame(route, composable)
    }

    fun pop() {
        assert(stack.size > 1) { "Can not pop start element!" }
        stack.removeAt(stack.size - 1)
    }

    data class Frame(
        val route: NavigationRoute,
        val composable: @Composable (NavigationRoute) -> Unit,
        var content: (@Composable () -> Unit)? = null,
    )
}
