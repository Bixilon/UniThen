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

package de.bixilon.unithen.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainBenchmarks {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "de.bixilon.unithen"
    ) {
        startActivityAndWait()

        device.wait(Until.hasObject(By.text("Check In (Show)")), 1000)
        device.findObject(By.text("Check In (Show)")).click()
        device.wait(Until.hasObject(By.text("Marie Zimmer")), 1000)
        device.findObject(By.text("Marie Zimmer")).click()
        device.wait(Until.hasObject(By.text("Start")), 1000)
        device.pressBack()

        device.wait(Until.hasObject(By.text("Courses")), 1000)
        device.findObject(By.text("Courses")).click()
        device.wait(Until.hasObject(By.text("First course")), 1000)

        device.findObject(By.text("Check In (Scan)")).click()
        device.wait(Until.hasObject(By.text("Gustaf Maier")), 5000)
    }
}
