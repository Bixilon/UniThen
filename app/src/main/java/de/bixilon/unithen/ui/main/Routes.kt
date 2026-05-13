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

package de.bixilon.unithen.ui.main

import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.Site
import de.bixilon.unithen.ui.navigation.NavigationRoute

object MainRoute : NavigationRoute
object SetupRoute : NavigationRoute
object AddAccountRoute : NavigationRoute
data class ReauthenticateRoute(val site: Site) : NavigationRoute
object AboutRoute : NavigationRoute

object AccountsRoute : NavigationRoute

data class AccountDetailsRoute(val account: Account) : NavigationRoute

object CoursesRoute : NavigationRoute
data class CourseDetailsRoute(val course: Course) : NavigationRoute


object DebugRoute : NavigationRoute


object SettingsRoute : NavigationRoute

data class CrashRoute(val exception: Throwable) : NavigationRoute
