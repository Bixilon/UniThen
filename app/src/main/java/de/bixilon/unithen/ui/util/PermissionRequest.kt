/*
 * UniThen - Compose QR Camera
 * Copyright (C) 2026 Moritz Zwerger
 *
 * Ported from Activity to Jetpack Compose
 */

package de.bixilon.unithen.ui.util

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun usePermissionRequest(permission: String): Boolean {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted = it })

    LaunchedEffect(Unit) { launcher.launch(permission) }


    return granted
}
