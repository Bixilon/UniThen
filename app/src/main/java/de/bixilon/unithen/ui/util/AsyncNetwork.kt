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

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.fasterxml.jackson.core.JacksonException
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.ui.main.CrashRoute
import de.bixilon.unithen.ui.main.ReauthenticateRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
fun <T> useAsyncNetwork(account: Account?, block: suspend (T) -> Unit): (T) -> Unit {
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current
    val context = LocalContext.current


    val invoke = { args: T ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                block.invoke(args)
            } catch (_: AuthenticationException) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Please reauthenticate!", Toast.LENGTH_SHORT).show() }
                if (account != null) {
                    storage.accounts.logout(account)
                    navigation.navigate(ReauthenticateRoute(storage.sites[account.site]!!))
                }
            } catch (error: JacksonException) {
                error.printStackTrace()
                navigation.navigate(CrashRoute(error))
            } catch (error: IOException) {
                error.printStackTrace()
                withContext(Dispatchers.Main) { Toast.makeText(context, "Network error!", Toast.LENGTH_SHORT).show() }
            } catch (error: Throwable) {
                error.printStackTrace()
                navigation.navigate(CrashRoute(error))
            }
        }

        Unit
    }

    return invoke
}
