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

import androidx.compose.runtime.*
import de.bixilon.unithen.ui.main.CrashRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun <T> rememberAsync(invokable: suspend () -> T): T? {
    val navigation = LocalNavigation.current
    var value by remember { mutableStateOf<T?>(null) }


    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                value = invokable.invoke()
            } catch (error: Throwable) {
                error.printStackTrace()
                navigation.navigate(CrashRoute(error))
            }
        }
    }


    return value
}
