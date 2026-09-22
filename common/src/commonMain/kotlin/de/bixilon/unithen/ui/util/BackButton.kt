package de.bixilon.unithen.ui.util

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.unithen.ui.navigation.LocalNavigation


@Composable
fun BoxScope.BackButton() {
    BackButton(Modifier.align(Alignment.TopStart).padding(4.dp))
}

@Composable
fun BackButton(modifier: Modifier = Modifier) {
    val navigation = catchAll { LocalNavigation.current } ?: return
    if (navigation.size <= 1) return

    IconButton({ navigation.pop() }, modifier) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "back")
    }
}
