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

package de.bixilon.unithen.ui.main

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.bixilon.unithen.storage.sql.SqlHelper.Companion.executeBatch
import de.bixilon.unithen.ui.FastCheckinActivity
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage


@Composable
fun DebugScreen() {
    val navigator = LocalNavigation.current

    Column {
        Text("Debug menu:")
        Button({ navigator.navigate(SetupRoute) }) { Text("Open setup") }

        val context = LocalContext.current
        val storage = LocalStorage.current

        Button({ navigator.navigate(MainRoute) }) { Text("Main") }
        Button({ storage.helper.writableDatabase.executeBatch("dummy") }) { Text("Fill database with junk") }
        Button({ throw IllegalStateException("It crashed!") }) { Text("Crash") }

        Button({ context.startActivity(Intent(context, FastCheckinActivity::class.java)) }) { Text("Fast Check In") }
    }
}
