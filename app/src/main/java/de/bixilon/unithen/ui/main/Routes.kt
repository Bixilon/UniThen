package de.bixilon.unithen.ui.main

import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.ui.navigation.NavigationRoute

object SetupRoute : NavigationRoute
object HomeRoute : NavigationRoute
object SitesRoute : NavigationRoute

data class AuthenticationRoute(val site: Site) : NavigationRoute
