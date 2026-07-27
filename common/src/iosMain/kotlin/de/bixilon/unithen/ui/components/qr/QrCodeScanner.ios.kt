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

package de.bixilon.unithen.ui.components.qr

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import de.bixilon.kutil.cast.CastUtil.cast
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.unithen.settings.Settings
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.ui.error.ErrorBox
import de.bixilon.unithen.ui.util.camera.useCameraPermission
import de.bixilon.unithen.ui.util.i18n
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSData
import platform.Foundation.valueForKeyPath
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import unithen.common.generated.resources.Res
import unithen.common.generated.resources.scan_camera_permission

@OptIn(ExperimentalForeignApi::class)
private class QrUiView(
    val highQuality: Boolean,
    val onResult: (Set<QrCodeResult>) -> Unit,
    val onError: (Throwable) -> Unit,
) : UIView(frame = CGRectZero.readValue()) {
    private val session = AVCaptureSession()
    private val preview = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }
    private var errored = false

    init {
        layer.addSublayer(preview)

        try {
            setup()
        } catch (error: Throwable) {
            errored = true
            error.printStackTrace()
            stop()
            onError(error)
        }
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        preview.frame = bounds
    }

    override fun didMoveToWindow() {
        super.didMoveToWindow()

        if (window != null && !session.running && !errored) {
            session.startRunning()
        }
    }

    override fun removeFromSuperview() {
        stop()
        super.removeFromSuperview()
    }

    private fun setup() {
        val device = AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo).first { it.nullCast<AVCaptureDevice>()?.position == AVCaptureDevicePositionBack }?.nullCast<AVCaptureDevice>() ?: throw IllegalStateException("No video devices!")
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, error = null) ?: throw IllegalStateException("No capture input devices!")
        if (!session.canAddInput(input)) throw IllegalStateException("Can not add input device!")

        session.addInput(input)

        if (highQuality && session.canSetSessionPreset(AVAssetExportPresetHighestQuality)) {
            session.sessionPreset = AVAssetExportPresetHighestQuality
        }

        val output = AVCaptureMetadataOutput()
        if (!session.canAddOutput(output)) throw IllegalStateException("Can not add output!")

        session.addOutput(output)

        output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)

        output.setMetadataObjectsDelegate(QrDelegate(onResult), dispatch_get_main_queue())
    }

    fun stop() {
        if (session.isRunning()) {
            session.stopRunning()
        }
    }
}

private class QrDelegate(val onResult: (Set<QrCodeResult>) -> Unit) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(output: AVCaptureOutput, didOutputMetadataObjects: List<*>, fromConnection: AVCaptureConnection) {
        val code = didOutputMetadataObjects.mapNotNull {
            if (it !is AVMetadataMachineReadableCodeObject) return@mapNotNull null
            // https://stackoverflow.com/questions/32429480/how-to-read-binary-qr-code-with-avfoundation

            val raw = it.valueForKeyPath("_internal.basicDescriptor").cast<Map<*, *>>()["BarcodeRawData"] as NSData

            return@mapNotNull QrCodeResult(raw.toByteArray())
        }.toSet()

        onResult.invoke(code)
    }
}

@Composable
actual fun QrCameraPreview(modifier: Modifier, onResult: (Set<QrCodeResult>) -> Unit) {
    val permission = useCameraPermission()

    if (!permission) {
        CameraMessage(modifier, Res.string.scan_camera_permission.i18n())
        return
    }
    var error by remember { mutableStateOf<String?>(null) }

    if (error != null) {
        ErrorBox("Camera error: $error")
        return
    }

    val highResolution by rememberSetting(Settings.SCAN_QR_HIGH_RESOLUTION)

    UIKitView(
        modifier = modifier,
        factory = { QrUiView(highResolution, onResult) { error = it.message ?: it::class.simpleName } },
    )
}
