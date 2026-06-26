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
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.bixilon.kutil.enums.ValuesEnum
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@Composable
fun <T> rememberSetting(key: Preferences.Key<T>, default: T): MutableState<T> {
    val store = SETTINGS
    val scope = rememberCoroutineScope()

    val current = remember { store.state.map { it[key] } }.collectAsState(null)
    val initial = remember { store[key] }

    return remember {
        object : MutableState<T> {
            override var value
                get() = current.value ?: initial ?: default
                set(next) {
                    scope.launch { store.set(key, next) }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { this.value = it }
        }
    }
}


@Composable
@JvmName("rememberBooleanSetting")
fun rememberSetting(setting: Setting<Boolean>): MutableState<Boolean> {
    return rememberSetting(booleanPreferencesKey(setting.key), setting.default)
}

@Composable
@JvmName("rememberIntSetting")
fun rememberSetting(setting: Setting<Int>): MutableState<Int> {
    return rememberSetting(intPreferencesKey(setting.key), setting.default)
}

@Composable
@JvmName("rememberStringSetting")
fun rememberSetting(setting: Setting<String>): MutableState<String> {
    return rememberSetting(stringPreferencesKey(setting.key), setting.default)
}


@Composable
@JvmName("rememberEnumSetting")
fun <T : Enum<T>> rememberSetting(setting: Setting<T>, values: ValuesEnum<T>): MutableState<T> {
    val raw = rememberSetting(stringPreferencesKey(setting.key), setting.default.name)


    return remember {
        object : MutableState<T> {
            override var value: T
                get() = values.getOrNull(raw.value) ?: setting.default
                set(newValue) {
                    raw.value = newValue.name
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { this.value = it }
        }
    }
}
