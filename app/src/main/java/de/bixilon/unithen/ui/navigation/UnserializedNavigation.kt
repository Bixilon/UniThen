package de.bixilon.unithen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument


class UnserializedNavigation(
    val start: NavigationRoute,
    val controller: NavController,
) {
    val map: MutableMap<Int, NavigationRoute> = HashMap() // TODO: memory leak
    private var id = 0
    var host: NavGraphBuilder? = null

    inline fun <reified T : NavigationRoute> composable3(crossinline compose: @Composable (T) -> Unit) {
        val id = T::class.java.typeName
        host!!.composable("$id/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
            val data = map[it.arguments!!.getInt("id")]!!
            compose.invoke(data as T)
        }
    }


    fun navigate(route: NavigationRoute) {
        val id = id++
        map[id] = route
        controller.navigate(route::class.java.typeName + "/$id")
    }
}
