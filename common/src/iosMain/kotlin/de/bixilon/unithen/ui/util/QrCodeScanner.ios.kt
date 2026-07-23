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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
private class QrUiView(onResult: (Set<ScannedQrCode>) -> Unit) : UIView(frame = CGRectZero.readValue()) {
    private val session = AVCaptureSession()
    private val previewLayer = AVCaptureVideoPreviewLayer(session = session)

    private val delegate = QrDelegate(onResult)

    init {
        setupCamera()

        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer.frame = bounds
    }

    private fun setupCamera() {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, error = null) as AVCaptureDeviceInput
        if (!session.canAddInput(input)) return stop()

        session.addInput(input)

        val output = AVCaptureMetadataOutput()

        if (!session.canAddOutput(output)) return stop()

        session.addOutput(output)

        output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)

        output.setMetadataObjectsDelegate(delegate, dispatch_get_main_queue())

        session.startRunning()
    }

    fun stop() {
        session.stopRunning()
    }
}

private class QrDelegate(val onResult: (Set<ScannedQrCode>) -> Unit) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(output: AVCaptureOutput, didOutputMetadataObjects: List<*>, fromConnection: AVCaptureConnection) {
        val code = didOutputMetadataObjects.mapNotNull { (it as? AVMetadataMachineReadableCodeObject)?.stringValue?.let { ScannedQrCode(it) } }.toSet()

        onResult.invoke(code)
    }
}

@Composable
actual fun QrCameraPreview(modifier: Modifier, onResult: (Set<ScannedQrCode>) -> Unit) {
    UIKitView(
        modifier = modifier,
        factory = {
            QrUiView(onResult)
        },
        onRelease = { it.stop() }
    )
}
