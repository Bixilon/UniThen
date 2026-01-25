package de.bixilon.unithen.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavigation = staticCompositionLocalOf<Navigator> { throw IllegalStateException("No local navigator!") }
