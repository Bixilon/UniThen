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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.containers.TextCard
import de.bixilon.unithen.ui.main.CourseDetailsRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.storage.rememberStorageAsync
import de.bixilon.unithen.ui.sync.SyncEngineCompleteEffect
import de.bixilon.unithen.ui.sync.SyncEngineStartedEffect
import de.bixilon.unithen.ui.sync.status.SyncStatusIndicator
import de.bixilon.unithen.ui.sync.useSyncEngine
import de.bixilon.unithen.ui.util.i18n
import de.bixilon.unithen.ui.util.useToast
import de.bixilon.unithen.ui.util.verticalScroll
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.courses_synchronize_done
import unithen.common.generated.resources.courses_synchronize_started
import unithen.common.generated.resources.courses_title


@Composable
fun CoursesScreen() {
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current

    val courseCount = rememberStorage { courses.count }
    val events = rememberStorageAsync { events.all().sortedByDescending { it.start } } ?: emptyList() // TODO: sort in database

    val toast = useToast()

    val sync = useSyncEngine { syncCourses() }

    SyncEngineStartedEffect(sync) {
        toast.invoke(Res.string.courses_synchronize_started, true)
    }
    SyncEngineCompleteEffect(sync) {
        if (it.synchonized == it.total) {
            toast.invoke(Res.string.courses_synchronize_done)
        }
    }

    Box {
        Screen {
            ScreenTitle(Res.string.courses_title.i18n(courseCount))

            PullToRefreshBox(sync.active, modifier = Modifier.weight(1.0f), onRefresh = { sync.invoke() }) {
                val state = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(state),
                    state = state,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    storage.notify.intValue // listens for changes
                    for (event in events) {
                        val courses = storage.courses.get(event = event).sortedBy { it.name }
                        if (courses.isEmpty()) continue

                        item(key = "e" + event.id) {
                            val site = rememberStorage { sites[event.site]!! } // TODO: Section?
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (site.icon != null) {
                                    AsyncImage(
                                        site.icon,
                                        contentDescription = "Site icon",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Text(event.name + " (${courses.size}):")
                            }
                        }
                        items(items = courses, key = Course::id) { course ->
                            val tutor = rememberStorage { accounts.isTutor(course) }
                            val color = if (tutor) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            TextCard(course.name, color = color, modifier = Modifier.clickable { navigation.navigate(CourseDetailsRoute(course)) })
                        }
                    }
                }
            }
        }

        SyncStatusIndicator(sync, Modifier.align(Alignment.TopEnd))
    }
}
