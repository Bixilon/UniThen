package de.bixilon.unithen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.bixilon.kutil.enums.ValuesEnum

@Composable
actual fun rememberSetting(setting: Setting<Boolean>): MutableState<Boolean> {
    return remember { mutableStateOf(setting.default) }
}

@Composable
actual fun <T : Enum<T>> rememberSetting(setting: Setting<T>, values: ValuesEnum<T>): MutableState<T> {
    return remember { mutableStateOf(setting.default) }
}

@Composable
actual fun rememberSetting(setting: Setting<Int>): MutableState<Int> {
    return remember { mutableStateOf(setting.default) }
}

@Composable
actual fun rememberSetting(setting: Setting<String>): MutableState<String> {
    return remember { mutableStateOf(setting.default) }
}

