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

package de.bixilon.unithen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.UniThen.STORAGE
import de.bixilon.unithen.debug.DebugMainActivity
import de.bixilon.unithen.settings.store.LocalSettingsStore
import de.bixilon.unithen.settings.store.NSUserDefaultsSettingsStore
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.theme.UniThenTheme
import platform.UIKit.UIViewController


@Suppress("UNUSED")
fun IosMainActivity(): UIViewController = ComposeUIViewController {
    UniThenTheme {
        Scaffold(
            modifier = Modifier.imePadding(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CompositionLocalProvider(
                    LocalStorage provides STORAGE,
                    LocalSettingsStore provides NSUserDefaultsSettingsStore,
                ) {
                    if (RuntimeInfo.debug) DebugMainActivity() else CommonMainActivity()
                }
            }
        }
    }
}
