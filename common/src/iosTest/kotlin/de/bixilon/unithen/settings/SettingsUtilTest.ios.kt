package de.bixilon.unithen.settings

import de.bixilon.unithen.settings.store.NSUserDefaultsSettingsStore
import de.bixilon.unithen.settings.store.SettingsStore

actual fun createSettingsStore(): SettingsStore = NSUserDefaultsSettingsStore
