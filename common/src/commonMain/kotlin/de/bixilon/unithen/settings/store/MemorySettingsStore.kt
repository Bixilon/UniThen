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

package de.bixilon.unithen.settings.store

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.unithen.settings.AbstractSetting
import de.bixilon.unithen.settings.EnumSetting
import de.bixilon.unithen.settings.Setting

class MemorySettingsStore : SettingsStore {
    val settings = mutableMapOf<AbstractSetting<*>, MutableState<*>>()

    @Composable
    override fun createBoolean(setting: Setting<Boolean>): MutableState<Boolean> {
        return settings.getOrPut(setting) { mutableStateOf(setting.default) }.cast()
    }

    @Composable
    override fun createInt(setting: Setting<Int>): MutableState<Int> {
        return settings.getOrPut(setting) { mutableStateOf(setting.default) }.cast()
    }

    @Composable
    override fun createString(setting: Setting<String>): MutableState<String> {
        return settings.getOrPut(setting) { mutableStateOf(setting.default) }.cast()
    }

    @Composable
    override fun <T : Enum<T>> createEnum(setting: EnumSetting<T>): MutableState<T> {
        return settings.getOrPut(setting) { mutableStateOf(setting.default) }.cast()
    }
}
