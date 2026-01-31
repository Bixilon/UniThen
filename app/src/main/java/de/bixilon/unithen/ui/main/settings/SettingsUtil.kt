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

package de.bixilon.unithen.ui.main.settings

import androidx.compose.runtime.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@Composable
fun <T> rememberSetting(key: Preferences.Key<T>, default: T): MutableState<T> {
    val store = SETTINGS.store
    val scope = rememberCoroutineScope()

    val value by store.data.map { it[key] }.collectAsState(initial = null)

    return remember {
        object : MutableState<T> {
            override var value: T
                get() = value ?: default
                set(newValue) {
                    scope.launch { store.edit { it[key] = newValue } }
                }

            override fun component1() = value ?: default
            override fun component2(): (T) -> Unit = { this.value = it }
        }
    }
}


@Composable
@JvmName("renemberBooleanSetting")
fun rememberSetting(setting: Setting<Boolean>): MutableState<Boolean> {
    return rememberSetting(booleanPreferencesKey(setting.key), setting.default)
}

@Composable
@JvmName("renemberIntSetting")
fun rememberSetting(setting: Setting<Int>): MutableState<Int> {
    return rememberSetting(intPreferencesKey(setting.key), setting.default)
}
