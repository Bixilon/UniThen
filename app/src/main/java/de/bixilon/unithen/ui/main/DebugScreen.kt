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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.sql.SqlHelper.Companion.executeBatch
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.useAsyncNetwork
import java.util.*


private fun SqlStorage.insert1000Users() = transaction {
    for (i in 0 until 1000) {
        val userId = 930 + i
        val uuid = UUID(userId.toLong(), userId.toLong())
        this.insert("INSERT INTO users(id, site, uuid, firstname, lastname) VALUES(?, 901, ?,?,?)", userId, uuid, "User", "#${i}")
        this.insert("INSERT INTO course_enrolled(user, course) VALUES(?, 901)", userId)
    }
}

@Composable
fun DebugScreen() {
    val storage = LocalStorage.current
    val navigator = LocalNavigation.current

    Column {
        Text(
            "Debug menu",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Button({ navigator.navigate(SetupRoute) }) { Text("Open setup") }


        Button({ navigator.navigate(MainRoute) }) { Text("Main") }
        Button({ storage.helper.writableDatabase.executeBatch("dummy") }) { Text("Initiate dummy database") }
        Button({ storage.insert1000Users() }) { Text("Insert 1000 users") }
        Button({ throw IllegalStateException("It crashed!") }) { Text("Crash") }

        var progress by remember { mutableStateOf<String?>(null) }

        val synchronize = useAsyncNetwork<Unit>(null) {
            try {
                CheckInUtil.synchronizeDatabase(storage) { current, total -> progress = "$current/$total" }
                progress = null
            } catch (error: Throwable) {
                progress = "Error: ${error.message}"
                throw error
            }
        }
        Button({
            progress = "..."
            synchronize.invoke(Unit)
        }, enabled = progress == null) { Text(if (progress != null) "Synchronizing $progress" else "Synchronize checkins") }
    }
}
