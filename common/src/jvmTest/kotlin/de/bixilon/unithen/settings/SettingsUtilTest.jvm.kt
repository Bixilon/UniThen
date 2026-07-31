package de.bixilon.unithen.settings

import de.bixilon.unithen.settings.store.MemorySettingsStore
import de.bixilon.unithen.settings.store.SettingsStore

actual fun createSettingsStore(): SettingsStore = MemorySettingsStore()
