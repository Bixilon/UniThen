package de.bixilon.unithen.ui.main.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.main.appointment.AppointmentCard


@Composable
fun CourseScreen(course: Course) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(DataStorage.STORAGE.appointments.get(course)) { item ->
            AppointmentCard(item)
        }
    }
}
