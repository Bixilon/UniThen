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

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.CoreImage.*
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy

// TODO: This is so trashy

@Suppress("CAST_NEVER_SUCCEEDS")
fun String.nsdata(): NSData? {
    return (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray =
    ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }

fun UIImage.asImageBitmap(): ImageBitmap {
    val data = UIImagePNGRepresentation(this)!!
    val bytes = data.toByteArray()

    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

@OptIn(ExperimentalForeignApi::class)
actual fun encodeQr(data: String): ImageBitmap {
    val filter = CIFilter.QRCodeGenerator()

    if (filter is CIQRCodeGeneratorProtocol) {
        filter.correctionLevel = "H"
        filter.message = data.nsdata()!!
    }

    val output = filter.outputImage ?: throw IllegalArgumentException("Failed to generate QR image")

    val context = CIContext.context()

    val cgImage = context.createCGImage(output, fromRect = output.extent) ?: throw IllegalArgumentException("Failed to create CGImage")

    val uiImage = UIImage.imageWithCGImage(cgImage)

    return uiImage.asImageBitmap()
}
