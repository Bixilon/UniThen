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

package de.bixilon.unithen.ui.main.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.Section
import de.bixilon.unithen.ui.containers.SectionTitle
import de.bixilon.unithen.ui.storage.LocalStorage


@Composable
private fun EnrolledCard(user: User) {

    val color = when {
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = user.firstname + " " + user.lastname,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun CourseEnrolled(course: Course) {
    val storage = LocalStorage.current
    val users by remember { storage.users.stateOf { this.getEnrolled(course) } }

    if (users.isEmpty()) return

    // TODO: scrollbar
    Section {
        SectionTitle("Enrolled (${users.size})")

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items = users, key = User::id) { EnrolledCard(it) }
        }
    }
}
