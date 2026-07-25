package de.bixilon.unithen.settings

import androidx.compose.runtime.*
import de.bixilon.kutil.enums.ValuesEnum
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDefaultsDidChangeNotification


private operator fun NSUserDefaults.contains(setting: Setting<*>): Boolean {
    return objectForKey(setting.key) != null
}

private operator fun NSUserDefaults.get(setting: Setting<Boolean>): Boolean {
    if (setting !in this) return setting.default

    return boolForKey(setting.key)
}

private operator fun NSUserDefaults.get(setting: Setting<Int>): Int {
    if (setting !in this) return setting.default

    return integerForKey(setting.key).toInt()
}

private operator fun NSUserDefaults.get(setting: Setting<String>): String {
    if (setting !in this) return setting.default

    return stringForKey(setting.key) ?: setting.default
}

private operator fun <T : Enum<T>> NSUserDefaults.get(setting: Setting<T>, values: ValuesEnum<T>): T {
    if (setting !in this) return setting.default

    return stringForKey(setting.key)?.let { values[it] } ?: setting.default
}

@Composable
private fun NSUserDefaults.Observer(callback: () -> Unit) {
    DisposableEffect(Unit) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = NSUserDefaultsDidChangeNotification,
            `object` = this@Observer,
            queue = null,
            usingBlock = { callback.invoke() }
        )

        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }
}


private fun <T> MutableState<T>.createState(onChange: (T) -> Unit): MutableState<T> {
    value

    return object : MutableState<T> {
        override var value: T
            get() = this@createState.value
            set(next) {
                if (value == next) return
                onChange(next)
            }

        override fun component1() = this.value
        override fun component2(): (T) -> Unit = { this.value = it }
    }
}

@Composable
actual fun rememberSetting(setting: Setting<Boolean>): MutableState<Boolean> {
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

    val defaults = NSUserDefaults.standardUserDefaults
    val value = remember { mutableStateOf(defaults[setting]) }

    defaults.Observer { value.value = defaults[setting] }

    return remember { value.createState { defaults.setBool(it, setting.key) } }
}

@Composable
actual fun <T : Enum<T>> rememberSetting(setting: Setting<T>, values: ValuesEnum<T>): MutableState<T> {
    val defaults = NSUserDefaults.standardUserDefaults
    val value = remember { mutableStateOf(defaults[setting, values]) }

    defaults.Observer { value.value = defaults[setting, values] }

    return remember { value.createState { defaults.setObject(it.name, setting.key) } }
}

@Composable
actual fun rememberSetting(setting: Setting<Int>): MutableState<Int> {
    val defaults = NSUserDefaults.standardUserDefaults
    val value = remember { mutableStateOf(defaults[setting]) }

    defaults.Observer { value.value = defaults[setting] }

    return remember { value.createState { defaults.setInteger(it.toLong(), setting.key) } }
}

@Composable
actual fun rememberSetting(setting: Setting<String>): MutableState<String> {
    val defaults = NSUserDefaults.standardUserDefaults
    val value = remember { mutableStateOf(defaults[setting]) }

    defaults.Observer { value.value = defaults[setting] }

    return remember { value.createState { defaults.setObject(it, setting.key) } }
}

