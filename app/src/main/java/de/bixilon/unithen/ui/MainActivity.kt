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

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.UniThen
import de.bixilon.unithen.debug.DebugMainActivity
import de.bixilon.unithen.settings.store.LocalSettingsStore
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.theme.UniThenTheme
import de.bixilon.unithen.ui.util.LocalUrlIntent
import de.bixilon.unithen.util.AndroidUtil.activity


class MainActivity : ComponentActivity() {
    private val url = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)


        url.value = intent.data?.toString()

        setContent {
            BackHandler { activity?.finish() }
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
                            LocalStorage provides UniThen.STORAGE,
                            LocalSettingsStore provides UniThen.SETTINGS,
                            LocalUrlIntent provides url.value,
                        ) {
                            if (BuildConfig.DEBUG) DebugMainActivity() else CommonMainActivity()
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        url.value = intent.data?.toString()
    }
}
