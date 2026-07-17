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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.UIKit.*

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun useToast(): ToastInvoker { // thanks: https://github.com/DaaniDev/Toastix/blob/master/composeApp/src/iosMain/kotlin/ShowToastMsg.ios.kt
    return object : ToastInvoker {
        override suspend fun invoke(message: String, long: Boolean) {
            val toast = UILabel(frame = CGRectMake(0.0, 0.0, UIScreen.mainScreen.bounds.useContents { size.width } - 40, 35.0))

            toast.center = CGPointMake(UIScreen.mainScreen.bounds.useContents { size.width } / 2, UIScreen.mainScreen.bounds.useContents { size.height } - 100.0)
            toast.textAlignment = NSTextAlignmentCenter
            toast.backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.6)
            toast.textColor = UIColor.whiteColor
            toast.text = message
            toast.alpha = 1.0
            toast.layer.cornerRadius = 15.0
            toast.clipsToBounds = true

            UIApplication.sharedApplication.keyWindow?.rootViewController?.view?.addSubview(toast)

            UIView.animateWithDuration(
                if (long) 10.0 else 5.0,
                delay = 0.1,
                options = UIViewAnimationOptionCurveEaseOut,
                animations = { toast.alpha = 0.0 },
                completion = { if (it) toast.removeFromSuperview() })
        }
    }
}
