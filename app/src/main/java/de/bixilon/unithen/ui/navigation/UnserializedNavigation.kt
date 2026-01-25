package de.bixilon.unithen.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.composable


class UnserializedNavigation(
    val controller: NavHostController,
) {
    val map: MutableMap<Int, NavigationRoute> = HashMap() // TODO: memory leak
    private var id = 0


    inner class Builder(
        val builder: NavGraphBuilder,
    ) {

        inline fun <reified T : NavigationRoute> composable(crossinline compose: @Composable (T) -> Unit) {
            val id = T::class.java.typeName
            builder.composable("$id/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                val data = remember { map[it.arguments!!.getInt("id")] }
                compose.invoke(data as T)
            }
        }
    }


    @Composable
    fun NavHost(
        startDestination: NavigationRoute,
        modifier: Modifier = Modifier,
        contentAlignment: Alignment = Alignment.TopStart,
        route: String? = null,
        enterTransition:
        (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
            {
                fadeIn(animationSpec = tween(700))
            },
        exitTransition:
        (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
            {
                fadeOut(animationSpec = tween(700))
            },
        popEnterTransition:
        (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
            enterTransition,
        popExitTransition:
        (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
            exitTransition,
        sizeTransform:
        (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? =
            null,
        builder: Builder.() -> Unit,
    ) {
        val id = id++
        map[id] = startDestination

        androidx.navigation.compose.NavHost(
            this.controller,
            remember(route, startDestination.toRoute(id)) {
                this.controller.createGraph(startDestination.toRoute(id), route, { builder.invoke(Builder(this)) })
            },
            modifier,
            contentAlignment,
            enterTransition,
            exitTransition,
            popEnterTransition,
            popExitTransition,
            sizeTransform
        )
    }


    fun navigate(route: NavigationRoute) {
        val id = id++
        map[id] = route
        controller.navigate(route.toRoute(id))
    }


    private fun NavigationRoute.toRoute(id: Int) = this::class.java.typeName + "/$id"
}
