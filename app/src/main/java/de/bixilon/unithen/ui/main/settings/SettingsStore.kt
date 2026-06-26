package de.bixilon.unithen.ui.main.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(context: Context) {
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
}

lateinit var SETTINGS: SettingsStore
