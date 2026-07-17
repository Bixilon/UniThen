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

package de.bixilon.unithen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.bixilon.kutil.enums.ValuesEnum

@Composable
@JvmName(name = "rememberBooleanSetting")
actual fun rememberSetting(setting: Setting<Boolean>): MutableState<Boolean> {
    return remember { mutableStateOf(setting.default) }
}

@Composable
@JvmName(name = "rememberIntSetting")
actual fun rememberSetting(setting: Setting<Int>): MutableState<Int> {
    return remember { mutableStateOf(setting.default) }
}

@Composable
@JvmName(name = "rememberStringSetting")
actual fun rememberSetting(setting: Setting<String>): MutableState<String> {
    return remember { mutableStateOf(setting.default) }
}

@Composable
@JvmName(name = "rememberEnumSetting")
actual fun <T : Enum<T>> rememberSetting(setting: Setting<T>, values: ValuesEnum<T>): MutableState<T> {
    return remember { mutableStateOf(setting.default) }
}
