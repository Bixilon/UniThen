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

package de.bixilon.unithen.ui.main.checkin.scan.qr

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.util.verticalScrollWithBar

@Composable
fun ScanInstructions(courses: Collection<Course>) {
    val courses = courses + courses + courses + courses + courses + courses + courses + courses + courses + courses
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 45.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        InfoContainer(Modifier.alpha(0.7f), horizontalAlignment = Alignment.CenterHorizontally, color = MaterialTheme.colorScheme.secondaryContainer) {

            if (courses.size == 1) {
                Text(
                    text = courses.first().name,
                    style = MaterialTheme.typography.headlineMedium,
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp)
                        .verticalScrollWithBar()
                ) {
                    for (course in courses) {
                        Text(
                            text = course.name,
                            style = if (courses.size > 5) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
