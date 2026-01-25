package de.bixilon.unithen.ui.main

import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.ui.navigation.NavigationRoute

object Home : NavigationRoute
object Sites : NavigationRoute

data class AuthRoute(val site: Site) : NavigationRoute
