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

package de.bixilon.unithen.ui.main.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.Section
import de.bixilon.unithen.ui.containers.SectionTitle
import de.bixilon.unithen.ui.containers.TextCard
import de.bixilon.unithen.ui.main.CourseDetailsRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage

@Composable
fun AccountDetailsScreen(account: Account) {
    val storage = LocalStorage.current
    val site = remember { storage.sites[account.site]!! }
    val courses by remember { storage.courses.stateOf { this[account].sortedBy { it.name } } } // TODO: better sort

    Column(modifier = Modifier.padding(16.dp)) {
        InfoContainer {
            Text(
                text = "${account.firstname} ${account.lastname}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${site.name} (${site.url})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Section {
            SectionTitle("Courses (${courses.size})")


            val navigation = LocalNavigation.current
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items = courses, key = Course::id) { course -> TextCard(course.name, Modifier.clickable { navigation.navigate(CourseDetailsRoute(course)) }) }
            }
        }
    }
}
