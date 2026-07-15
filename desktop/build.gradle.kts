import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    sourceSets {
        jvmMain.dependencies {
            implementation(project(":common"))
            implementation(project(":debug"))
            implementation(compose.desktop.currentOs)
            implementation(libs.compose.material3)
            implementation(libs.androidx.navigationevent.desktop)
            implementation(libs.androidx.navigationevent.compose.desktop)

            implementation("org.xerial:sqlite-jdbc:3.53.2.0")
            runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.11.0")
        }
    }
}

compose.desktop {
    application {
        mainClass = "de.bixilon.unithen.UniThenKt"

        nativeDistributions {
            packageName = "UniThen"
            packageVersion = project.extra.get("version").toString()
        }
    }
}
