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

package de.bixilon.unithen.ui.sync.status

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.ui.sync.SyncEngineCompleteEffect
import de.bixilon.unithen.ui.sync.SyncEngineHook
import de.bixilon.unithen.ui.sync.SyncEngineStartedEffect
import de.bixilon.unithen.ui.sync.status.SyncIndicatorUtil.isDeterminate
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.state.rememberStateOf
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.sync_dialog_dismiss


@Composable
fun SyncStatusDialog(hook: SyncEngineHook, title: String, description: String) {
    var visible by rememberStateOf { false }

    SyncEngineCompleteEffect(hook) { visible = false }
    SyncEngineStartedEffect(hook) { visible = true }

    if (!hook.active) return
    if (!visible) return
    val progress = hook.progress


    AlertDialog(
        confirmButton = {},
        dismissButton = { Button({ visible = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondaryContainer)) { Text(Res.string.sync_dialog_dismiss.i18n()) } },
        onDismissRequest = { visible = false },
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (progress != null && progress.isDeterminate) {
                    CircularProgressIndicator(progress = { progress.synchonized.toFloat() / progress.total })
                } else {
                    CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (progress != null && progress.isDeterminate) {
                    Text(description + "(${progress.synchonized}/${progress.total})")
                } else {
                    Text(description)
                }
            }
        },
    )
}
