package de.bixilon.unithen.ui.navigation

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import de.bixilon.kutil.cast.CastUtil.cast
import java.util.Stack
import kotlin.reflect.KClass


class UnserializedNavigation(
    private val start: NavigationRoute,
) {
    private val stack = Stack<Frame>()
    private val routes = HashMap<KClass<out NavigationRoute>, @Composable (NavigationRoute) -> Unit>()
    private var observe: (() -> Unit)? = null

    class Builder(
        val routes: HashMap<KClass<out NavigationRoute>, @Composable (NavigationRoute) -> Unit>,
    ) {

        inline fun <reified T : NavigationRoute> composable(noinline compose: @Composable (T) -> Unit) {
            routes[T::class] = compose.cast()
        }
    }

    fun Context.findActivity(): ComponentActivity? = when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    @Composable
    fun Host(builder: Builder.() -> Unit) {
        val instance = remember { Builder(routes).apply(builder); navigate(start); "" }
        var size by remember { mutableIntStateOf(1) }

        DisposableEffect(Unit) {
            this@UnserializedNavigation.observe = { size = stack.size }

            onDispose {
                this@UnserializedNavigation.observe = null
            }
        }
        instance.isEmpty()

        val context = LocalContext.current
        BackHandler { if (stack.size > 1) pop() else context.findActivity()?.finish() }

        val frame = remember(size) { stack.peek() }

        frame.composable(frame.route)
    }

    fun navigate(route: NavigationRoute) {
        val composable = routes[route::class] ?: throw IllegalStateException("No route registered for $route!")

        stack += Frame(route, composable)
        observe?.invoke()
    }

    fun pop() {
        stack.pop()
        observe?.invoke()
    }

    data class Frame(
        val route: NavigationRoute,
        val composable: @Composable (NavigationRoute) -> Unit,
    )
}
