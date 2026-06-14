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

package de.bixilon.unithen.ui.main.checkin.present

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.unithen.R
import de.bixilon.unithen.storage.types.Account
import de.bixilon.unithen.storage.types.Appointment
import de.bixilon.unithen.storage.types.Course
import de.bixilon.unithen.ui.containers.*
import de.bixilon.unithen.ui.main.PresentQrRoute
import de.bixilon.unithen.ui.navigation.LocalNavigation
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.util.i18n


@Composable
fun PresentQrAccountSelector(course: Course, appointment: Appointment, accounts: List<Account>) {
    val storage = LocalStorage.current
    val navigation = LocalNavigation.current

    val site = storage.sites[course.site]!!

    Screen {
        ScreenTitle(R.string.present_choose_account_title.i18n())

        InfoContainer {
            InfoPair(R.string.course.i18n(), course.name)
            InfoPair(R.string.site.i18n(), site.name)
        }

        Spacer(Modifier.height(8.dp))


        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(accounts, key = Account::id) {
                TextCard(it.fullname, Modifier.clickable { navigation.navigate(PresentQrRoute(it, course, appointment)) })
            }
        }
    }
}
