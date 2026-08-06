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
import androidx.compose.runtime.remember
import de.bixilon.unithen.settings.store.LocalSettingsStore
import kotlin.jvm.JvmName


@Composable
@JvmName("rememberBooleanSetting")
fun rememberSetting(setting: Setting<Boolean>): MutableState<Boolean> {
    val supported = remember { isSettingSupported(setting) }
    if (!supported) return remember {
        object : MutableState<Boolean> {
            override var value
                get() = false
                set(next) = Unit

            override fun component1() = value
            override fun component2(): (Boolean) -> Unit = { }
        }
    }
    return LocalSettingsStore.current.createBoolean(setting)
}

@Composable
@JvmName("rememberIntSetting")
fun rememberSetting(setting: Setting<Int>) = LocalSettingsStore.current.createInt(setting)

@Composable
@JvmName("rememberStringSetting")
fun rememberSetting(setting: Setting<String>) = LocalSettingsStore.current.createString(setting)

@Composable
@JvmName("rememberEnumSetting")
fun <T : Enum<T>> rememberSetting(setting: EnumSetting<T>) = LocalSettingsStore.current.createEnum(setting)
