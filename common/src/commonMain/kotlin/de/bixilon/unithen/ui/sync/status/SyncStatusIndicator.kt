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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.sync.SyncEngineProgress
import de.bixilon.unithen.ui.sync.SyncEngineHook
import de.bixilon.unithen.ui.sync.status.SyncIndicatorUtil.isDeterminate
import de.bixilon.unithen.ui.util.state.rememberStateOf
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private enum class SyncStatus {
    SUCCESS,
    WARNING,
    ERROR,
    ;
}

@Composable
private fun CicleIndicator(status: SyncStatus, modifier: Modifier, hide: Boolean) {
    var dismissed by rememberStateOf { true }

    LaunchedEffect(status, hide) {
        dismissed = false
        val duration = when (status) {
            SyncStatus.SUCCESS -> 3.seconds
            SyncStatus.WARNING -> 5.seconds
            SyncStatus.ERROR -> 10.seconds
        }
        delay(duration)
        dismissed = true
    }
    val color = when (status) {
        SyncStatus.SUCCESS -> Color.Green
        SyncStatus.WARNING -> Color.Yellow
        SyncStatus.ERROR -> MaterialTheme.colorScheme.error
    }

    if (hide && dismissed) return

    Canvas(modifier = modifier.size(12.dp)) {
        drawCircle(color = color)
    }
}

@Composable
private fun RunningIndicator(status: SyncStatus?, progress: SyncEngineProgress?) {
    val color = when (status) {
        SyncStatus.WARNING -> Color.Yellow
        SyncStatus.ERROR -> MaterialTheme.colorScheme.error
        else -> ProgressIndicatorDefaults.circularColor
    }

    val modifier = Modifier.size(24.dp)


    if (progress != null && progress.isDeterminate) {
        CircularProgressIndicator(progress = { progress.synchonized.toFloat() / progress.total }, modifier = modifier, color = color)
    } else {
        CircularProgressIndicator(modifier = modifier, color = color)
    }
}

@Composable
fun SyncStatusIndicator(hook: SyncEngineHook, modifier: Modifier = Modifier, text: Boolean = false, hide: Boolean = true) {
    var hidden by rememberStateOf { true }
    var status by rememberStateOf(SyncStatus.SUCCESS)

    LaunchedEffect(hook) {
        if (hook.active) {
            hidden = false
        }
        val progress = hook.progress

        when {
            progress.errored > 0 -> status = SyncStatus.ERROR
            progress.warnings > 0 -> status = SyncStatus.WARNING
            progress.completed == 0 -> status = SyncStatus.SUCCESS
            progress.synchonized == progress.total -> status = SyncStatus.SUCCESS
        }
    }
    if (hidden) return

    if (hook.active) {
        val progress = hook.progress
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (progress.total > 0 && text) {
                Text("${progress.completed}/${progress.total}")
            }

            RunningIndicator(status, progress)
        }
    } else {
        CicleIndicator(status, modifier, hide)
    }
}
