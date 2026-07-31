package de.bixilon.unithen.settings

import androidx.test.platform.app.InstrumentationRegistry
import de.bixilon.unithen.settings.store.SettingsStore
import de.bixilon.unithen.settings.store.SharedPreferencesSettingsStore

actual fun createSettingsStore(): SettingsStore = SharedPreferencesSettingsStore(InstrumentationRegistry.getInstrumentation().context)
