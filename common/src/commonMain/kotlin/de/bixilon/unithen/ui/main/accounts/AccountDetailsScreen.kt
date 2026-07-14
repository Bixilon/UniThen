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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.containers.*
import de.bixilon.unithen.ui.main.CourseDetailsRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.verticalScroll
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.courses_title

@Composable
fun AccountDetailsScreen(account: Account) {
    val site = rememberStorage { sites[account.site]!! }
    val courses = rememberStorage { courses[account].sortedBy { it.name } } // TODO: better sort

    Screen {
        InfoContainer {
            Text(
                text = account.fullname,
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
            SectionTitle(Res.string.courses_title.i18n(courses.size))


            val navigation = LocalNavigation.current
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(state),
                state = state,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(items = courses, key = Course::id) { course -> TextCard(course.name, Modifier.clickable { navigation.navigate(CourseDetailsRoute(course)) }) }
            }
        }
    }
}
