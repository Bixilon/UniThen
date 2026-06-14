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

package de.bixilon.unithen.ui.main.settings.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.R
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useAppRestart
import de.bixilon.unithen.ui.util.useToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DatabaseCleanupDialog(dismiss: () -> Unit) {
    val restart = useAppRestart()
    val storage = LocalStorage.current
    val toast = useToast()

    LaunchedEffect(Unit) {
        try {
            storage.clearCache()
            storage.cleanup()
            toast.invoke(R.string.database_cleanup_success)
            restart.invoke()
        } finally {
            withContext(Dispatchers.Main) { dismiss.invoke() }
        }
    }

    AlertDialog(
        confirmButton = {},
        onDismissRequest = {},
        icon = { Icon(Icons.Filled.CleaningServices, "cleaning") },
        title = { Text(R.string.database_cleanup_title.i18n()) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(R.string.settings_entrypoint_description.i18n())
            }
        },
    )
}
