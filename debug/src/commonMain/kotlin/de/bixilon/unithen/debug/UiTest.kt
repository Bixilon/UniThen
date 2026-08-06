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

package de.bixilon.unithen.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.unithen.settings.EnumSetting
import de.bixilon.unithen.settings.Setting
import de.bixilon.unithen.settings.rememberSetting
import de.bixilon.unithen.ui.components.qr.QrCode
import de.bixilon.unithen.ui.containers.ScreenTitle
import de.bixilon.unithen.ui.main.settings.types.BooleanSetting
import de.bixilon.unithen.ui.main.settings.types.EnumSetting
import de.bixilon.unithen.ui.util.effects.RepeatedEffect
import de.bixilon.unithen.ui.util.verticalScrollWithBar
import kotlin.time.Duration.Companion.seconds

val A = Setting("test_a", true)
val B = Setting("test_b", false)
val C = EnumSetting("test_c", EnumTest.A, EnumTest)

enum class EnumTest {
    A,
    B,
    C,
    ;

    companion object : ValuesEnum<EnumTest> {
        override val VALUES = values()
        override val NAME_MAP = names()
    }
}


@Composable
private fun SettingsTest() {
    Column {
        ScreenTitle("Settings")

        Text("Both setting groups should be synchronized, A should toggle every second.")

        var a by rememberSetting(A)
        var c by rememberSetting(C)

        RepeatedEffect(1.seconds) { a = !a }
        RepeatedEffect(5.seconds) { c = EnumTest.next(c) }

        BooleanSetting(A, "A", "")
        BooleanSetting(A, "A", "")
        BooleanSetting(B, "B", "")
        BooleanSetting(B, "B", "")

        EnumSetting(C, "C", "")
        EnumSetting(C, "C", "")
        EnumSetting(C, "C", "")
    }
}

@Composable
private fun QrTest() {
    Column {
        ScreenTitle("QR")
        QrCode(
            "hello world",
            Modifier
                .width(300.dp)
                .height(300.dp)
        )
    }
}

@Composable
fun UiTestScreen() {
    Column(modifier = Modifier.verticalScrollWithBar()) {
        SettingsTest()
        QrTest()
    }
}
