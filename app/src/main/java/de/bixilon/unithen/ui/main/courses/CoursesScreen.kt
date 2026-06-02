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

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.api.graphql.util.CourseFetcher.fetch
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.containers.TextCard
import de.bixilon.unithen.ui.main.CourseDetailsRoute
import de.bixilon.unithen.ui.main.CrashRoute
import de.bixilon.unithen.ui.main.ReauthenticateRoute
import de.bixilon.unithen.ui.main.add.toBitmap
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun CoursesScreen() {
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current
    var refreshing by remember { mutableStateOf(false) }

    val courseCount = rememberStorage { courses.count }
    val events = rememberStorage { events.all().sortedByDescending { it.start } }

    val context = LocalContext.current
    Screen {
        ScreenTitle("Courses ($courseCount)")

        PullToRefreshBox(refreshing, modifier = Modifier.weight(1.0f), onRefresh = {
            refreshing = true
            CoroutineScope(Dispatchers.IO).launch {
                var loginSite: Key? = null
                var caught: Throwable? = null

                storage.accounts.all().forEach {
                    try {
                        storage.fetch(it, false)
                    } catch (_: AuthenticationException) {
                        storage.accounts.logout(it)
                        loginSite = it.site
                    } catch (error: Throwable) {
                        caught = error
                    }
                }
                if (loginSite != null) {
                    withContext(Dispatchers.Main) { Toast.makeText(context, "Please reauthenticate!", Toast.LENGTH_SHORT).show() }
                    navigation.navigate(ReauthenticateRoute(storage.sites[loginSite]!!))
                } else {
                    withContext(Dispatchers.Main) { Toast.makeText(context, "Courses refreshed!", Toast.LENGTH_SHORT).show() }
                }
                if (caught != null) {
                    navigation.navigate(CrashRoute(caught))
                }
                withContext(Dispatchers.Main) { refreshing = false }
            }
        }) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (event in events) {
                    val courses = storage.courses.get(event = event).sortedBy { it.name } // TODO: Cache
                    if (courses.isEmpty()) continue

                    item {
                        val site = rememberStorage { sites[event.site]!! } // TODO: Section?
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val bitmap = remember(site.icon) { site.icon?.toBitmap()?.asImageBitmap() }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
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
                    items(items = courses, key = Course::id) { course -> TextCard(course.name, Modifier.clickable { navigation.navigate(CourseDetailsRoute(course)) }) }
                }
            }
        }
    }
}
