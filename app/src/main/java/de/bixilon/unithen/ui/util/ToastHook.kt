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
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ToastInvoker {
    suspend operator fun invoke(message: String, long: Boolean = false)
    suspend operator fun invoke(@StringRes message: Int, long: Boolean = false)
}

@Composable
fun useToast(): ToastInvoker {
    val resources = LocalResources.current
    val context = LocalContext.current

    return object : ToastInvoker {
        override suspend fun invoke(message: String, long: Boolean) {
            withContext(Dispatchers.Main) { Toast.makeText(context, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show() }
        }

        override suspend fun invoke(message: Int, long: Boolean) = invoke(resources.getString(message, long))
    }
}
