package de.bixilon.unithen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.RuntimeInfo.RuntimeInfo0
import de.bixilon.unithen.storage.sql.NativeSQLiteHelper
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.theme.UniThenTheme
import platform.UIKit.UIViewController

val STORAGE = SqlStorage(NativeSQLiteHelper("uninow"))

@Suppress("UNUSED")
fun IosMainActivity(): UIViewController = ComposeUIViewController {
    RuntimeInfo0.actual = object : RuntimeInfo {
        override val debug get() = false
    }
    UniThenTheme {
        Scaffold(
            modifier = Modifier.imePadding(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        top = innerPadding.calculateTopPadding(),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = 0.dp
                    )
            ) {
                CompositionLocalProvider(
                    LocalStorage provides STORAGE,
                ) {
                    CommonMainActivity()
                }
            }
        }
    }
}
