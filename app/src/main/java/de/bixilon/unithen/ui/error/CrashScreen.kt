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

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bixilon.unithen.BuildConfig
import de.bixilon.unithen.R
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.api.graphql.http.GraphQlException
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.main.UpdateChecker
import de.bixilon.unithen.ui.util.Copy2Clipboard
import de.bixilon.unithen.ui.util.i18n
import java.io.IOException
import java.net.UnknownHostException


fun formatDetails(error: Throwable): String? = when (error) {
    is IOException -> error.message + "\nDo you have internet?"
    is AuthenticationException -> "Unauthenticated!"
    is GraphQlException -> error.format()
    else -> null
}


@Composable
fun CrashScreen(message: String?, exception: Throwable) {
    if (exception is UnknownHostException) {
        return SimpleErrorScreen(R.string.error_network.i18n(), exception.message)
    }
    Screen(
        modifier = Modifier
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ErrorBox(message ?: "Something went wrong!", "This should not have happened. Please contact the app developer and include the full text below.")

        val details = formatDetails(exception)
        if (details != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .padding(8.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = details,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val trace = remember { exception.stackTraceToString() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())) {
                SelectionContainer {
                    Text(
                        text = trace,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Copy2Clipboard(trace, modifier = Modifier.align(Alignment.TopEnd))
        }

        if (!BuildConfig.DEBUG) {
            Text("You can try checking for updates below, the crash is most likely fixed in the latest realease.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                UpdateChecker()
                Spacer(Modifier.width(8.dp))
                ReportErrorButton(trace)
            }
        }
    }
}
