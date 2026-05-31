package de.bixilon.unithen.ui.fast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.containers.InfoContainer
import de.bixilon.unithen.ui.containers.InfoPair
import de.bixilon.unithen.ui.containers.Screen
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.PresentQrRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage


@Composable
fun FastCheckinAccountSelector(course: Course, appointment: Appointment, accounts: List<Account>) {
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current

    val site = storage.sites[course.site]!!

    Screen {
        ScreenTitle("Please choose account")

        InfoContainer {
            InfoPair("Course", course.name)
            InfoPair("Site", site.name)
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(accounts, key = Account::id) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navigation.navigate(PresentQrRoute(item, course, appointment)) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
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
}
