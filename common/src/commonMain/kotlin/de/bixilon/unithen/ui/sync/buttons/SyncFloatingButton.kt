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

package de.bixilon.unithen.ui.sync.buttons

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import de.bixilon.unithen.ui.sync.SyncEngineHook
import de.bixilon.unithen.ui.sync.status.SyncIndicatorUtil.isDeterminate

@Composable
fun SyncFloatingButton(hook: SyncEngineHook, icon: ImageVector, onClick: () -> Unit = {}) {
    val color = if (hook.active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer

    FloatingActionButton({
        if (!hook.active) {
            hook.invoke(force = true)
        }
        onClick.invoke()
    }, containerColor = color) {
        if (hook.active) {
            val progress = hook.progress

            if (progress.isDeterminate) {
                CircularProgressIndicator(progress = { progress.synchonized.toFloat() / progress.total })
            } else {
                CircularProgressIndicator()
            }
        }
        Icon(icon, "sync")
    }
}
