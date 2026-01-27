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

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import de.bixilon.unithen.UniThen
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.navigation.LocalNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun MainScreen() {
    val navigator = LocalNavigation.current

    LaunchedEffect(Unit) {
        if (DataStorage.STORAGE.accounts.count == 0) {
            navigator.navigate(SetupRoute)
        }
    }


    Column {
        Text("Welcome!")
        Button({ navigator.navigate(SetupRoute) }) { Text("Open setup") }
        var refreshing by remember { mutableStateOf(false) }

        val context = LocalContext.current

        Button(enabled = !refreshing, onClick = {
            refreshing = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    UniThen.updateCourses()
                    withContext(Dispatchers.Main) { Toast.makeText(context, "Courses refreshed!", Toast.LENGTH_SHORT).show() }
                } catch (error: Throwable) {
                    withContext(Dispatchers.Main) { Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show() }
                }
                withContext(Dispatchers.Main) { refreshing = false }
            }
        }) { if (refreshing) Text("Refreshing...") else Text("Refresh courses") }


        Button({ navigator.navigate(CoursesRoute) }) { Text("Courses") }

        SitesScreen()
    }
}
