package de.bixilon.unithen.ui.main.checkin.scan.qr.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.functions.FunctionUtil.letIf
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.Settings.SCAN_QR_AUTO_SCAN
import de.bixilon.unithen.settings.isSettingSupported
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_EARLY_DURATION
import de.bixilon.unithen.storage.types.Appointment.Companion.CHECKIN_LATE_DURATION
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.containers.FloatingActionButtons
import de.bixilon.unithen.ui.containers.SafeBox
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.ScanAnyRoute
import de.bixilon.unithen.ui.main.checkin.scan.attendees.UserFilterX
import de.bixilon.unithen.ui.main.checkin.scan.attendees.rememberUserFilter
import de.bixilon.unithen.ui.main.checkin.scan.errors.CheckInError
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.storage.rememberStorage
import de.bixilon.unithen.ui.storage.rememberStorageAsync
import de.bixilon.unithen.ui.sync.LocalSyncEngine
import de.bixilon.unithen.ui.sync.useSyncEngine
import de.bixilon.unithen.ui.util.unfocusOnTap
import de.bixilon.unithen.ui.util.useTime
import de.bixilon.unithen.ui.util.useToast
import de.bixilon.unithen.ui.util.verticalScroll
import org.jetbrains.compose.resources.getString
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.scan_error_rejected_message

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
            color = MaterialTheme.colorScheme.onSurface
        )

        Box {
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
fun UserCard(modifier: Modifier, user: User, appointments: List<Appointment>, alternative: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.letIf(alternative) { copy(alpha = 0.8f) }),
        modifier = modifier,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.padding(12.dp).weight(1.0f),
                text = user.fullname,
                style = MaterialTheme.typography.bodyMedium,
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
fun UserSearchScreen() {
    val navigation = LocalNavigation.current
    val filter = rememberUserFilter()


    val time = useTime()
    val appointments = rememberStorageAsync(time) { appointments.getInRange(time - CHECKIN_LATE_DURATION, time + CHECKIN_EARLY_DURATION, canceled = false, tutor = true) } ?: return

    val users = rememberStorageAsync(appointments, filter.search, filter.sort, filter.order) { users.getAppointmenUsers(time - CHECKIN_LATE_DURATION, time + CHECKIN_EARLY_DURATION, canceled = false, tutor = true, filter.search, filter.sort, filter.order) }


    // TODO: Update enrolled/attendees

    Screen(Modifier.unfocusOnTap()) {
        ScreenTitle("Search for users")

        UserFilterX(filter)

        val state = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.fillMaxSize().verticalScroll(state),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = state,
        ) {
            item("_") { Spacer(Modifier.height(1.dp)) } // https://stackoverflow.com/questions/74320761/compose-lazycolumn-key-messes-up-scrolling-when-sorting-the-items
            users?.let { users -> itemsIndexed(items = users, key = { _, it -> it.id }) { index, it -> UserCard(modifier(), it, appointments, index % 2 != 0) } }
        }
    }

    SafeBox {
        FloatingActionButtons {
            var search by rememberSetting(Settings.SCAN_USER_SEARCH)

            FloatingActionButton({ search = false }) {
                Icon(Icons.Filled.SearchOff, "choose")
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
