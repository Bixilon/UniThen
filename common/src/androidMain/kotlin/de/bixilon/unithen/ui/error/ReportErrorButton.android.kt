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

package de.bixilon.unithen.ui.error

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.bixilon.unithen.BuildInfo

@Composable
actual fun useSendCrashMail(): (stack: String) -> Unit {
    val context = LocalContext.current

    return {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(CRASH_ADDRESS))
            putExtra(Intent.EXTRA_SUBJECT, "UniThen Crash")
            putExtra(Intent.EXTRA_TEXT, "Hi there,\nApp version: ${BuildInfo.VERSION}\nPlease see the exception below:\n\n${it}\n\n\nCan you please fix this issue?\nThanks!")
        }


        context.startActivity(Intent.createChooser(intent, "Pick an Email provider"))
    }
}
