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

package de.bixilon.unithen.ui.main.checkin.scan.attendees

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.string.WhitespaceUtil.trimWhitespaces
import de.bixilon.unithen.ui.main.settings.Settings
import de.bixilon.unithen.ui.main.settings.rememberSetting


class UserFilter(_search: MutableState<String>, _sort: MutableState<AttendeeSort>, _order: MutableState<Order>) {
    var search by _search
    var sort by _sort
    var order by _order
}

@Composable
fun rememberUserFilter(): UserFilter {
    val sort = rememberSetting(Settings.ATTENDEE_SORT, AttendeeSort)
    val order = rememberSetting(Settings.ATTENDEE_ORDER, Order)

    return remember { UserFilter(mutableStateOf(""), sort, order) }
}

@Composable
fun UserFilterX(filter: UserFilter) {
    val search = rememberTextFieldState()

    LaunchedEffect(search.text) { filter.search = search.text.toString().trimWhitespaces() }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            search,
            lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier.weight(1.0f, true),
            placeholder = { Text("Search...") },
            trailingIcon = {
                if (search.text.isNotBlank()) {
                    IconButton({ search.clearText() }) { Icon(Icons.Default.Clear, "clear") }
                }
            },
        )

        Box {
            var expanded by remember { mutableStateOf(false) }
            IconButton({ expanded = !expanded }) { Icon(Icons.AutoMirrored.Filled.List, "sort") }


            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                for (item in AttendeeSort) {
                    var color = MenuDefaults.itemColors()
                    if (filter.sort == item) {
                        color = color.copy(textColor = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(item.label)) },
                        colors = color,
                        onClick = { filter.sort = item; expanded = false },
                    )
                }
            }
        }

        IconButton({ filter.order = if (filter.order == Order.ASC) Order.DESC else Order.ASC }) {
            Icon(if (filter.order == Order.DESC) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, "order")
        }
    }
}
