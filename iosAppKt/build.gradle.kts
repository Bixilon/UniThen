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

import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_4)
        freeCompilerArgs.add("-Xwarning-level=NOTHING_TO_INLINE:disabled")
        freeCompilerArgs.add("-Xintrinsic-const-evaluation")
    }

    val xcfName = "unithen"

    iosSimulatorArm64 {
        binaries.framework {
            binaryOption("bundleId", "de.bixilon.unithen.ios")
            baseName = xcfName
        }
    }
    iosArm64 {
        binaries.framework {
            binaryOption("bundleId", "de.bixilon.unithen.ios")
            baseName = xcfName
        }
    }

    sourceSets {

        iosMain.dependencies {
            implementation(project(":common"))
            implementation(project(":debug"))
        }
    }
}
