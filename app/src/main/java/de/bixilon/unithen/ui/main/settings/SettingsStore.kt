package de.bixilon.unithen.ui.main.settings

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore


private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(context: Context) {
    val store = context.dataStore
}


lateinit var SETTINGS: SettingsStore
