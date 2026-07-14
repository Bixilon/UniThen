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

package de.bixilon.unithen.ui.auth

import android.webkit.WebSettings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.ui.util.rememberAsync
import kotlin.time.TimeSource

private var warmed = false

@Composable
fun WebViewWarmup() {
    if (warmed) return
    warmed = true

    val context = LocalContext.current

    // Thanks: https://groups.google.com/a/chromium.org/g/android-webview-dev/c/hjn1h7dBlH8
    rememberAsync {
        val start = TimeSource.Monotonic.markNow()
        catchAll { WebSettings.getDefaultUserAgent(context) }
        println("Webview preloaded: ${TimeSource.Monotonic.markNow() - start}")
    }
}

