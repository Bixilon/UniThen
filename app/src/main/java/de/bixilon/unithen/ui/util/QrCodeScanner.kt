/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.ui.util

import android.Manifest
import android.graphics.Rect
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import zxingcpp.BarcodeReader

@Composable
fun QrCameraPreview(modifier: Modifier = Modifier, onResult: (BarcodeReader.Result) -> Unit) {
    val permission = usePermissionRequest(Manifest.permission.CAMERA)

    if (!permission) return

    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current

    val requests = remember { MutableStateFlow<SurfaceRequest?>(null) }
    val reader = remember { BarcodeReader(BarcodeReader.Options(formats = setOf(BarcodeReader.Format.QR_CODE), tryRotate = true, tryDenoise = true)) }

    var provider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val request by requests.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        provider = ProcessCameraProvider.awaitInstance(context)
    }

    DisposableEffect(provider) {
        val provider = provider ?: return@DisposableEffect onDispose {}

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { requests.value = it }
        }

        val analyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    imageProxy.use {
                        val bitmap = it.toBitmap()

                        val rect = Rect(0, 0, bitmap.width, bitmap.height)

                        val results = reader.read(bitmap, rect)

                        results.forEach(onResult)
                    }
                }
            }

        provider.unbindAll()
        provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)

        onDispose {
            provider.unbindAll()
            requests.value = null
        }
    }

    request?.let {
        CameraXViewfinder(
            surfaceRequest = it,
            modifier = modifier.fillMaxSize()
        )
    }
}
