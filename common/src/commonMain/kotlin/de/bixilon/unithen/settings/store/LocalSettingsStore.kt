package de.bixilon.unithen.settings.store

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSettingsStore = staticCompositionLocalOf<SettingsStore> { throw IllegalStateException("No settings store set!") }

