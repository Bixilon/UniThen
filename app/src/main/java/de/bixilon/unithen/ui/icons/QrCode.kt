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

package de.bixilon.unithen.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Filled.QrCode: ImageVector
    get() {
        if (_qrCode != null) {
            return _qrCode!!
        }
        _qrCode = materialIcon(name = "Filled.QrCode") {
            materialPath {
                moveTo(3f, 11f)
                lineTo(11f, 11f)
                lineTo(11f, 3f)
                lineTo(3f, 3f)
                close()

                moveTo(5f, 5f)
                lineTo(9f, 5f)
                lineTo(9f, 9f)
                lineTo(5f, 9f)
                close()

                moveTo(3f, 21f)
                lineTo(11f, 21f)
                lineTo(11f, 13f)
                lineTo(3f, 13f)
                close()

                moveTo(5f, 15f)
                lineTo(9f, 15f)
                lineTo(9f, 19f)
                lineTo(5f, 19f)
                close()

                moveTo(13f, 3f)
                lineTo(13f, 11f)
                lineTo(21f, 11f)
                lineTo(21f, 3f)
                close()

                moveTo(19f, 9f)
                lineTo(15f, 9f)
                lineTo(15f, 5f)
                lineTo(19f, 5f)
                close()

                moveTo(19f, 19f)
                lineTo(21f, 19f)
                lineTo(21f, 21f)
                lineTo(19f, 21f)
                close()

                moveTo(13f, 13f)
                lineTo(15f, 13f)
                lineTo(15f, 15f)
                lineTo(13f, 15f)
                close()

                moveTo(15f, 15f)
                lineTo(17f, 15f)
                lineTo(17f, 17f)
                lineTo(15f, 17f)
                close()

                moveTo(13f, 17f)
                lineTo(15f, 17f)
                lineTo(15f, 19f)
                lineTo(13f, 19f)
                close()

                moveTo(15f, 19f)
                lineTo(17f, 19f)
                lineTo(17f, 21f)
                lineTo(15f, 21f)
                close()

                moveTo(17f, 17f)
                lineTo(19f, 17f)
                lineTo(19f, 19f)
                lineTo(17f, 19f)
                close()

                moveTo(17f, 13f)
                lineTo(19f, 13f)
                lineTo(19f, 15f)
                lineTo(17f, 15f)
                close()

                moveTo(19f, 15f)
                lineTo(21f, 15f)
                lineTo(21f, 17f)
                lineTo(19f, 17f)
                close()
            }
        }
        return _qrCode!!
    }

private var _qrCode: ImageVector? = null
