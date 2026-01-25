package de.bixilon.unithen.ui.main.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.Course
import de.bixilon.unithen.storage.DataStorage
import de.bixilon.unithen.ui.navigation.Navigator


@Composable
fun CourseCard(navigation: Navigator, course: Course) {
    Card(onClick = { /*navigation.navigate("/course/${course.id}")*/ }) {
        Text(course.name)
    }
}

@Composable
fun CoursesScreen(navigation: Navigator) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(DataStorage.STORAGE.courses.all()) { item ->
            CourseCard(navigation, item)
        }
    }
}
