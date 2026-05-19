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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.api.AuthenticatedUniNowApi
import de.bixilon.unithen.api.authentication.CookieAuthentication
import de.bixilon.unithen.api.graphql.http.AuthenticationException
import de.bixilon.unithen.api.graphql.http.GraphQlException
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.Key
import de.bixilon.unithen.storage.sql.SqlTable.Companion.stateOf
import de.bixilon.unithen.ui.main.CourseDetailsRoute
import de.bixilon.unithen.ui.main.CrashRoute
import de.bixilon.unithen.ui.main.ReauthenticateRoute
import de.bixilon.unithen.ui.main.add.toBitmap
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun CoursesScreen() {
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current
    var refreshing by remember { mutableStateOf(false) }

    val courseCount by remember { storage.courses.stateOf { count } }
    val events by remember { storage.events.stateOf { all().sortedByDescending { it.start } } }

    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            "Courses ($courseCount):",
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(Modifier.height(16.dp))

        PullToRefreshBox(refreshing, modifier = Modifier.weight(1.0f), onRefresh = {
            refreshing = true
            CoroutineScope(Dispatchers.IO).launch {
                var loginSite: Key? = null
                var caught: Throwable? = null

                storage.accounts.all().forEach {
                    try {
                        val site = storage.sites[it.site]!!
                        val api = AuthenticatedUniNowApi(site.url, CookieAuthentication(it.session))
                        val courses = api.postings(it.uuid) ?: return@forEach

                        storage.populate(site, it, courses)
                    } catch (_: AuthenticationException) {
                        storage.accounts.logout(it)
                        loginSite = it.site
                    } catch (error: GraphQlException) {
                        caught = error
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
                    val courses = storage.courses.get(event).sortedBy { it.name } // TODO: Cache
                    if (courses.isEmpty()) continue

                    item {
                        val site = remember { storage.sites[event.site]!! }
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
                    items(items = courses, key = Course::id) { course -> CourseCard(course) { navigation.navigate(CourseDetailsRoute(course)) } }
                }
            }
        }
    }
}
