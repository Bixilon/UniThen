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

package de.bixilon.unithen.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.api.errors.NetworkException
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.ui.main.CrashRoute
import de.bixilon.unithen.ui.main.ReauthenticateRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.state.rememberStateOf
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.getString
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.error_network
import unithen.common.generated.resources.error_reauthenticate

private val ACTIVE = AsyncNetworkState(true) { null }

data class AsyncNetworkState(
    val active: Boolean,
    val invoke: () -> Job?,
)

@Composable
fun useAsyncNetwork(block: suspend () -> Unit): AsyncNetworkState {
    var active by rememberStateOf { false }
    if (active) return ACTIVE

    val storage = catchAll { LocalStorage.current }
    val navigation = catchAll { LocalNavigation.current }
    val toast = useToast()

    val scope = remember { CoroutineScope(Dispatchers.IO) }

    val invoke = a@{
        if (active) return@a null
        scope.launch {
            try {
                active = true
                block.invoke()
            } catch (error: AuthenticationException) {
                toast.invoke(Res.string.error_reauthenticate)
                navigation?.navigate(ReauthenticateRoute(storage!!.sites[error.host]!!))
            } catch (error: NetworkException) {
                error.printStackTrace()
                toast.invoke(getString(Res.string.error_network, error.message ?: ""))
            } catch (error: Throwable) {
                error.printStackTrace()
                navigation?.navigate(CrashRoute(error))
            } finally {
                active = false
            }
        }
    }

    return AsyncNetworkState(active, invoke)
}
