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


import androidx.compose.ui.graphics.*
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder


val PAINT = Paint().apply { color = Color.White }

actual fun encodeQr(data: String): ImageBitmap {
    val matrix = Encoder.encode(data, ErrorCorrectionLevel.M).matrix

    val bitmap = ImageBitmap(matrix.width, matrix.height, ImageBitmapConfig.Alpha8)

    val canvas = Canvas(bitmap)

    for (x in 0 until matrix.width) {
        for (y in 0 until matrix.height) {
            if (matrix.get(x, y) != 1.toByte()) continue

            canvas.drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, PAINT)
        }
    }

    return bitmap
}
