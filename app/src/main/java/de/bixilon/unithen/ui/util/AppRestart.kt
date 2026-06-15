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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.bixilon.unithen.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// Thanks https://stackoverflow.com/questions/6609414/how-do-i-programmatically-restart-an-android-app
@Composable
fun useAppRestart(): () -> Unit {
    val context = LocalContext.current

    return {
        CoroutineScope(Dispatchers.Main).launch {
            val activity = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(context, 123456, activity, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            manager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pending)

            Runtime.getRuntime().exit(0) // TODO: broken
        }
    }
}
