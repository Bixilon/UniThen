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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.Section
import de.bixilon.unithen.ui.containers.SectionTitle
import de.bixilon.unithen.ui.containers.TextCard
import de.bixilon.unithen.ui.storage.rememberStorageAsync
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.verticalScroll
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.course_enrolled_title


@Composable
fun CourseEnrolled(course: Course) {
    val users = rememberStorageAsync(course) { users.getEnrolled(course) } ?: return

    if (users.isEmpty()) return

    Section {
        SectionTitle(Res.string.course_enrolled_title.i18n(users.size))

        val state = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.verticalScroll(state),
            state = state,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(items = users, key = User::id) { TextCard(it.fullname) }
        }
    }
}
