package de.bixilon.unithen.settings.store

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.unithen.settings.Setting

interface SettingsStore {

    @Composable
    fun createBoolean(setting: Setting<Boolean>): MutableState<Boolean>

    @Composable
    fun createInt(setting: Setting<Int>): MutableState<Int>

    @Composable
    fun createString(setting: Setting<String>): MutableState<String>

    @Composable
    fun <T : Enum<T>> createEnum(setting: Setting<T>, values: ValuesEnum<T>): MutableState<T>
}
