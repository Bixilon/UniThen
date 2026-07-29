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

package de.bixilon.unithen.ui.sync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import de.bixilon.unithen.api.errors.NetworkException
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.sync.SyncEngine
import de.bixilon.unithen.ui.main.CrashRoute
import de.bixilon.unithen.ui.main.ReauthenticateRoute
import de.bixilon.unithen.ui.navigation.Navigator

val LocalSyncEngine = staticCompositionLocalOf<SyncEngine> { throw IllegalStateException("No sync engine set!") }


@Composable
fun rememberSyncEngine(storage: SqlStorage, navigator: Navigator): SyncEngine {

    return remember {
        SyncEngine(storage) {
            when (it) {
                is NetworkException -> Unit
                is AuthenticationException -> navigator.navigate(ReauthenticateRoute(storage.sites[it.host]!!))
                else -> navigator.navigate(CrashRoute(it))
            }
        }
    }
}
