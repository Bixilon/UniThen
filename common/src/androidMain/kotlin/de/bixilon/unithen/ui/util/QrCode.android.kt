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


import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder


actual fun encodeQr(data: String): ImageBitmap {
    val matrix = Encoder.encode(data, ErrorCorrectionLevel.H, mapOf(
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.MARGIN to 16,
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
    )).matrix

    val bitmap = createBitmap(matrix.width, matrix.height, Bitmap.Config.ALPHA_8)

    for (x in 0 until matrix.width) {
        for (y in 0 until matrix.height) {
            if (matrix.get(x, y) != 1.toByte()) continue

            bitmap[x, y] = 0xFFFFFFFF.toInt()
        }
    }

    return bitmap.asImageBitmap()
}
