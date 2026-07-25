package de.bixilon.unithen.settings

import androidx.compose.runtime.*
import de.bixilon.kutil.enums.ValuesEnum
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDefaultsDidChangeNotification


private operator fun NSUserDefaults.contains(setting: Setting<*>): Boolean {
    return objectForKey(setting.key) == null
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


@Composable
actual fun rememberSetting(setting: Setting<Boolean>): MutableState<Boolean> {
    val defaults = NSUserDefaults.standardUserDefaults
    val store = remember { mutableStateOf(defaults[setting]) }

    defaults.Observer { store.value = defaults[setting] }

    LaunchedEffect(store) {
        defaults.setBool(store.value, setting.key)
    }

    return store
}

@Composable
actual fun <T : Enum<T>> rememberSetting(setting: Setting<T>, values: ValuesEnum<T>): MutableState<T> {
    val defaults = NSUserDefaults.standardUserDefaults
    val store = remember { mutableStateOf(defaults[setting, values]) }

    defaults.Observer { store.value = defaults[setting, values] }

    LaunchedEffect(store) {
        defaults.setObject(store.value.name, setting.key)
    }

    return remember {
        object : MutableState<T> {
            override var value: T
                get() = store.value
                set(next) {
                    store.value = next
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { this.value = it }
        }
    }
}

@Composable
actual fun rememberSetting(setting: Setting<Int>): MutableState<Int> {
    val defaults = NSUserDefaults.standardUserDefaults
    val store = remember { mutableStateOf(defaults[setting]) }

    defaults.Observer { store.value = defaults[setting] }

    LaunchedEffect(store) {
        defaults.setInteger(store.value.toLong(), setting.key)
    }

    return store
}

@Composable
actual fun rememberSetting(setting: Setting<String>): MutableState<String> {
    val defaults = NSUserDefaults.standardUserDefaults
    val store = remember { mutableStateOf(defaults[setting]) }

    defaults.Observer { store.value = defaults[setting] }

    LaunchedEffect(store) {
        defaults.setObject(store.value, setting.key)
    }

    return store
}

