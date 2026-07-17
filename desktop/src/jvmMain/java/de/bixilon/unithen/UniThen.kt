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

package de.bixilon.unithen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.*
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.unithen.debug.DebugMainActivity
import de.bixilon.unithen.storage.sql.JvmSqlHelper
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.ui.CommonMainActivity
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.theme.UniThenTheme
import java.nio.file.Path
import kotlin.io.path.div

val home = run {
    val user = System.getProperty("user.home")?.let { Path.of(it) } ?: throw IllegalStateException("Can not get user home!")

    return@run user / when (PlatformInfo.OS) {
        OSTypes.LINUX -> ".local/share/unithen"
        OSTypes.WINDOWS -> "AppData/Roaming/UniThen"
        OSTypes.MAC -> "Library/Application Support/UniThen"
        else -> ".unithen"
    }
}


val STORAGE by lazy { SqlStorage(JvmSqlHelper(home.toFile())) }

@Composable
fun ApplicationScope.UniThenApplication() {
    Window(
        onCloseRequest = ::exitApplication,
        title = "UniThen",
        state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
        ),
    ) {

        UniThenTheme {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    CompositionLocalProvider(
                        LocalStorage provides STORAGE,
                    ) {
                        if (RuntimeInfo.debug) DebugMainActivity() else CommonMainActivity()
                    }
                }
            }
        }
    }
}


fun main(args: Array<String>) {
    val debug = args.contains("--debug")
    RuntimeInfo.RuntimeInfo0.actual = object : RuntimeInfo {
        override val debug get() = debug
    }
    application { UniThenApplication() }
}
