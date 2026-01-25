package de.bixilon.unithen.ui.fast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.exception.Broken
import de.bixilon.unithen.storage.Account
import de.bixilon.unithen.storage.Appointment
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.main.CheckInScreen
import de.bixilon.unithen.ui.navigation.Navigator
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


@Preview
@Composable
fun FastCheckinNoAppointments() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(top = 50.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.errorContainer,
            tonalElevation = 2.dp,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "No upcoming appointments!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Are you sure you are there at the right time?",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun AppointmentCard(
    course: Course,
    appointment: Appointment,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = course.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${appointment.start} - ${appointment.end}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun FastCheckinAppointmentSelector(navigation: Navigator, appointments: List<Appointment>) {
    Column {
        Text(
            text = "Choose upcoming appointment",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(appointments) { item ->
                val course by remember { mutableStateOf(DataStorage.STORAGE.courses[item.course]!!) }
                AppointmentCard(course, item) { navigation.navigate(CheckInAppointment(course, item)) }
            }
        }
    }
}


@Composable
fun FastCheckinAccountSelector(navigation: Navigator, course: Course, appointment: Appointment, accounts: List<Account>) {
    Column {
        Text(
            text = "Choose upcoming appointment",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accounts) { item ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navigation.navigate(CheckInRoute(item, course, appointment)) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = item.firstname + " " + item.lastname,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    Column {
        Text(course.name)
        Text(appointment.start.toString())
        Text("Choose an account for check in...")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accounts) { item ->
                Card {
                    Text(item.firstname)
                    Text(item.lastname)
                }
            }
        }
    }
}

@Composable
fun FastCheckinAppointment(navigation: Navigator, course: Course, appointment: Appointment) {
    val accounts = remember { DataStorage.STORAGE.accounts[course] }

    when (accounts.size) {
        0 -> Broken("Unassociated data left in database!")
        1 -> CheckInScreen(accounts[0], course, appointment)
        else -> FastCheckinAccountSelector(navigation, course, appointment, accounts)
    }
}

@Composable
fun FastCheckInInScreen(navigation: Navigator) {
    LocalDateTime.now()
    val fixed = Instant.ofEpochSecond(1769446901).atZone(ZoneOffset.systemDefault()).toLocalDateTime()

    val time = fixed // TODO: debug
    val appointments by remember { mutableStateOf(DataStorage.STORAGE.appointments.getInRange(time.minusHours(1), time)) }

    when (appointments.size) {
        0 -> FastCheckinNoAppointments()
        1 -> FastCheckinAppointment(navigation, DataStorage.STORAGE.courses[appointments[0].course]!!, appointments[0])
        else -> FastCheckinAppointmentSelector(navigation, appointments)
    }
}
