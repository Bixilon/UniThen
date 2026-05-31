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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fasterxml.jackson.core.JacksonException
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.api.graphql.http.GraphQlException
import java.io.IOException


fun formatDetails(error: Throwable): String? = when (error) {
    is IOException if error !is JacksonException -> error.message + "\nDo you have internet?"
    is AuthenticationException -> "Unauthenticated!"
    is GraphQlException -> error.format()
    else -> null
}


@Composable
fun CrashScreen(message: String?, exception: Throwable) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ErrorBox(message ?: "Something went wrong!", "This should not have happened. Please contact the app developer if you don't know further.")

        val details = formatDetails(exception)
        if (details != null) {
            Spacer(modifier = Modifier.height(16.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                )
                .padding(8.dp)
        ) {
            SelectionContainer {
                Text(
                    text = exception.stackTraceToString(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
