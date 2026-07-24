package de.bixilon.unithen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import de.bixilon.unithen.RuntimeInfo
import de.bixilon.unithen.RuntimeInfo.RuntimeInfo0
import de.bixilon.unithen.storage.sql.NativeSQLiteHelper
import de.bixilon.unithen.storage.sql.SqlStorage
import de.bixilon.unithen.ui.storage.LocalStorage
import de.bixilon.unithen.ui.theme.UniThenTheme
import platform.UIKit.UIViewController

val STORAGE = SqlStorage(NativeSQLiteHelper(null))

fun IosMainActivity(): UIViewController = ComposeUIViewController {
    RuntimeInfo0.actual = object : RuntimeInfo {
        override val debug get() = false
    }
    UniThenTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
