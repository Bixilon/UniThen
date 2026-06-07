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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.ui.main.CrashRoute
import de.bixilon.unithen.ui.main.ReauthenticateRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

data class AsyncNetworkState<T>(
    val active: Boolean,
    val invoke: (T) -> Unit,
)

@Composable
fun <T> useAsyncNetwork(account: Account?, block: suspend (T) -> Unit): AsyncNetworkState<T> {
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current
    val toast = useToast()
    val active = remember { mutableStateOf(false) }

    if (active.value) return AsyncNetworkState(true, {})

    val invoke = { args: T ->
        CoroutineScope(Dispatchers.IO).launch {
            if (active.value) return@launch
            try {
                active.value = true
                block.invoke(args)
            } catch (_: AuthenticationException) {
                toast.invoke("Please reauthenticate!")
                if (account != null) {
                    storage.accounts.logout(account)
                    navigation.navigate(ReauthenticateRoute(storage.sites[account.site]!!))
                }
            } catch (error: IOException) {
                error.printStackTrace()
                toast.invoke("Network error: " + error.message)
            } catch (error: Throwable) {
                error.printStackTrace()
                navigation.navigate(CrashRoute(error))
            } finally {
                active.value = false
            }
        }

        Unit
    }

    return AsyncNetworkState(active.value, invoke)
}
