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

package de.bixilon.unithen.ui.main.checkin.scan

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.bixilon.unithen.storage.types.User
import de.bixilon.unithen.ui.storage.LocalStorage
import java.util.*


@Composable
fun QrScanConfirmScreen(user: User) {
    val (account, course, appointment) = LocalScanContext.current
    Text("Are you sure (${user.firstName} ${user.lastName}?")

    // TODO: Check if already checked in
}

@Composable
fun QrScanConfirmScreen(userId: UUID) {
    val (account, course, appointment) = LocalScanContext.current

    val storage = LocalStorage.current
    val site = storage.sites[storage.courses[appointment.course]!!.site]!!
    val user = storage.users[site, userId]

    if (user != null) {
        QrScanConfirmScreen(user)
        return
    }

    Text("Unknown user ${userId}!")
}
