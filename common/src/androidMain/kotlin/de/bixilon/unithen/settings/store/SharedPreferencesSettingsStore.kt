package de.bixilon.unithen.settings.store

import android.content.Context
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import de.bixilon.unithen.settings.EnumSetting
import de.bixilon.unithen.settings.Setting
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "settings")

class SharedPreferencesSettingsStore(context: Context) : SettingsStore {
    val store = context.dataStore
    val state = store.data

    suspend fun preload() {
        state.first()
    }

    operator fun <T> get(key: Preferences.Key<T>): T? {
        return runBlocking { state.map { it[key] }.first() }
    }

    suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        store.edit { it[key] = value }
    }

    @Composable
    private fun <T> create(key: Preferences.Key<T>, default: T): MutableState<T> {
        val scope = rememberCoroutineScope()

        val current = remember { state.map { it[key] } }.collectAsState(null)
        val initial = remember { this[key] }

        return remember {
            object : MutableState<T> {
                override var value
                    get() = current.value ?: initial ?: default
                    set(next) {
                        scope.launch { set(key, next) }
                    }

                override fun component1() = value
                override fun component2(): (T) -> Unit = { this.value = it }
            }
        }
    }


    @Composable
    override fun createBoolean(setting: Setting<Boolean>): MutableState<Boolean> {
        return create(booleanPreferencesKey(setting.key), setting.default)
    }

    @Composable
    override fun createInt(setting: Setting<Int>): MutableState<Int> {
        return create(intPreferencesKey(setting.key), setting.default)
    }

    @Composable
    override fun createString(setting: Setting<String>): MutableState<String> {
        return create(stringPreferencesKey(setting.key), setting.default)
    }

    @Composable
    override fun <T : Enum<T>> createEnum(setting: EnumSetting<T>): MutableState<T> {
        val raw = create(stringPreferencesKey(setting.key), setting.default.name)


        return remember(raw.value) {
            object : MutableState<T> {
                override var value: T
                    get() = setting.values.getOrNull(raw.value) ?: setting.default
                    set(newValue) {
                        raw.value = newValue.name
                    }

                override fun component1() = value
                override fun component2(): (T) -> Unit = { this.value = it }
            }
        }
    }

}
