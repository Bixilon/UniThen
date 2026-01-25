package de.bixilon.unithen.ui.navigation

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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


    inner class Builder(
    ) {
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
        BackHandler {
            if (stack.size > 1) pop()
            else context.findActivity()?.finish()
        }

        val frame = stack.last()

        var previousStackSize by remember { mutableIntStateOf(stack.size) }
        val isForward = stack.size > previousStackSize

        LaunchedEffect(stack.size) {
            previousStackSize = stack.size
            isNavigating = false
        }

        AnimatedContent(
            targetState = frame,
            transitionSpec = {
                if (isForward) {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                }
            }
        ) { target ->
            target.composable(target.route)
        }
    }

    fun navigate(route: NavigationRoute) {
        val composable = routes[route::class] ?: throw IllegalStateException("No route registered for $route!")

        if (isNavigating) {
            Log.v("NAV", "Ignoring route $route (already navigating)")
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
    )
}
