package de.bixilon.unithen.ui.main.checkin.scan.attendees

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.functions.FunctionUtil.letIf
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.Settings.SCAN_QR_AUTO_SCAN
import de.bixilon.unithen.settings.isSettingSupported
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_EARLY_DURATION
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_LATE_DURATION
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.*
import de.bixilon.unithen.ui.main.ScanAnyRoute
import de.bixilon.unithen.ui.main.checkin.scan.CheckInUtil.SYNC_BACKOFF_NORMAL
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.main.checkin.scan.qr.QrUpdateIndicator
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.storage.rememberStorageAsync
import de.bixilon.unithen.ui.sync.LocalSyncEngine
import de.bixilon.unithen.ui.sync.buttons.SyncFloatingButton
import de.bixilon.unithen.ui.sync.status.SyncStatusDialog
import de.bixilon.unithen.ui.sync.useRepeatedSyncEngine
import de.bixilon.unithen.ui.sync.useSyncEngine
import de.bixilon.unithen.ui.util.*
import org.jetbrains.compose.resources.getString
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.scan_error_rejected_message
import unithen.common.generated.resources.scan_synchronizing_attendees
import unithen.common.generated.resources.scan_users_title
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
private fun UserCourseEntry(user: User, appointment: Appointment) {
    val course = rememberStorage { courses[appointment.course] }
    if (!rememberStorage { users.isEnrolled(course, user) }) return


    val storage = LocalStorage.current
    val sync = LocalSyncEngine.current

    val attendee = rememberStorage { users.isAttendee(appointment, user) }
    val queue = rememberStorage { checkInQueue[appointment, user] }
    val active = sync.isQueueActive(user, appointment)

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = course.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Right,
        )

        Spacer(Modifier.width(2.dp))

        Box(contentAlignment = Alignment.Center) {
            when {
                attendee -> {
                    val toast = useToast()

                    val checkout = useSyncEngine {
                        try {
                            this@useSyncEngine.queue.checkOut(appointment, user)
                        } catch (error: CheckInError) {
                            toast.invoke(getString(Res.string.scan_error_rejected_message, error.error._i18n()) + " (${user.fullname})")
                        }
                    }

                    Checkbox(true, { checkout.invoke() }, enabled = !checkout.active)
                }

                queue != null -> {
                    if (active) {
                        CircularProgressIndicator()
                    }

                    IconButton({ storage.checkInQueue.delete(appointment, user) }) {
                        Icon(Icons.Filled.Clear, "remove")
                    }
                }

                else -> {
                    val toast = useToast()
                    val checkin = useSyncEngine {
                        try {
                            this@useSyncEngine.queue.checkIn(appointment, user)
                        } catch (error: CheckInError) {
                            toast.invoke(getString(Res.string.scan_error_rejected_message, error.error._i18n()) + " (${user.fullname})")
                        }
                    }

                    Checkbox(false, { checkin.invoke() }, enabled = !checkin.active)
                }
            }
        }
    }
}


@Composable
fun UserCard(modifier: Modifier, user: User, appointments: List<Appointment>, courses: List<Course>, alternative: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.letIf(alternative) { copy(alpha = 0.8f) }),
        modifier = modifier,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.padding(12.dp).weight(1.0f),
                text = user.fullname,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                for (appointment in appointments) {
                    UserCourseEntry(user, appointment)
                }
            }
        }
    }
}

@Composable
private fun Content(appointments: List<Appointment>, courses: List<Course>, users: List<User>) {
    val synchronize = useSyncEngine {
        async { this.enrolled.sync(courses) }
        async { this.attendees.sync(appointments) }
    }

    LaunchedEffect(Unit) {
        if (appointments.any { it.isAttendeesStale() } || courses.any { it.isEnrolledStale() }) {
            synchronize.invoke()
        }
    }

    PullToRefreshBox(synchronize.active, modifier = Modifier.fillMaxHeight(), onRefresh = { synchronize.invoke(force = true) }) {
        val state = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state),
            state = state,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 150.dp),
        ) {
            item("_") { Spacer(Modifier.height(1.dp)) } // https://stackoverflow.com/questions/74320761/compose-lazycolumn-key-messes-up-scrolling-when-sorting-the-items
            itemsIndexed(items = users, key = { _, it -> it.id }) { index, it -> UserCard(modifier(), it, appointments, courses, index % 2 != 0) }
        }
    }
}

@Composable
fun UserSearchScreen(appointments: List<Appointment>) {
    val navigation = LocalNavigation.current
    val filter = rememberUserFilter()


    val courses = rememberStorageAsync { appointments.map { courses[it.course] } }

    val time = useTime()
    val users = rememberStorageAsync(appointments, filter.search, filter.sort, filter.order) { users.getAppointmenUsers(time - CHECKIN_LATE_DURATION, time + CHECKIN_EARLY_DURATION, canceled = false, tutor = true, filter.search, filter.sort, filter.order) }


    val pending = rememberStorageAsync(appointments) { checkInQueue.getCount(appointments) } ?: 0
    val synchronize = useRepeatedSyncEngine(SYNC_BACKOFF_NORMAL + 1.minutes) {
        queue.sync(appointments)
    }

    val dialog = SyncStatusDialog(synchronize, Res.string.scan_synchronizing_attendees.i18n(), Res.string.scan_synchronizing_attendees.i18n(), manual = true)


    Screen(Modifier.unfocusOnTap()) {
        ScreenTitle(Res.string.scan_users_title.i18n())

        UserFilterX(filter)

        if (courses == null || users == null) {
            LoadingScreen()
        } else {
            Content(appointments, courses, users)
        }
    }

    SafeBox {
        QrUpdateIndicator(Modifier.align(Alignment.TopEnd).padding(4.dp), appointments)

        FloatingActionButtons {
            if (pending > 0) {
                DelayedContent(1.seconds) {
                    SyncFloatingButton(synchronize, Icons.Filled.Sync) { dialog.show() }
                }
            }

            var search by rememberSetting(Settings.SCAN_USER_SEARCH)

            FloatingActionButton({ search = false }) {
                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, "choose")
            }


            if (isSettingSupported(SCAN_QR_AUTO_SCAN)) {
                FloatingActionButton({ navigation.navigate(ScanAnyRoute) }) {
                    Icon(Icons.Filled.QrCodeScanner, "scan")
                }
            }
        }
    }
}


private fun LazyItemScope.modifier() = Modifier.fillMaxWidth().animateItem()
