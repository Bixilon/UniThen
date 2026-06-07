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

import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.kutil.string.WhitespaceUtil.removeMultipleWhitespaces
import de.bixilon.kutil.string.WhitespaceUtil.trimWhitespaces
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.4.0"
}


buildscript {
    dependencies {
        classpath(libs.kutil)
    }
}

fun getEnv(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

data class GitStatus(
    val commit: String,
    val branch: String,
    val clean: Boolean,
    val tag: String?,
)

fun loadGitFromEnv(): GitStatus? {
    val commit = getEnv("CI_COMMIT_SHA") ?: return null
    val branch = getEnv("CI_COMMIT_BRANCH") ?: return null
    val tag = getEnv("CI_COMMIT_TAG")
    return GitStatus(commit, branch, true, tag)
}

val FDROID = project.properties["fdroid"]?.toBoolean() ?: false

val hasGit by lazy { project.rootDir.resolve(".git").exists() }

fun executeGit(vararg args: String): String? {
    if (!hasGit) return null

    val process = ProcessBuilder()
        .command(*(arrayOf("git") + args))
        .directory(project.rootDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    if (process.waitFor() != 0) return null

    return process.inputStream.readAsString()
        .trimWhitespaces()
        .trim { it == '\n' || it == '\r' }
        .removeMultipleWhitespaces()
        .takeIf { it.isNotBlank() }
}

fun loadGitFromGit(): GitStatus? {
    val commit = executeGit("rev-parse", "HEAD") ?: return null
    val branch = executeGit("branch", "--show-current") ?: "master"
    val clean = if (FDROID) true else executeGit("status", "--porcelain") == null
    val tag = executeGit("describe", "--exact-match", "--tags")

    return GitStatus(commit, branch, clean, tag)
}

val git by lazy { ignoreAll { loadGitFromGit() } ?: loadGitFromEnv() }

android {
    namespace = "de.bixilon.unithen"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.bixilon.unithen"
        minSdk = 26
        targetSdk = 36
        versionCode = 3

        buildConfigField("String", "GIT_COMMIT", git?.commit?.let { "\"$it\"" }.toString())
        buildConfigField("String", "GIT_BRANCH", git?.branch?.let { "\"$it\"" }.toString())
        buildConfigField("String", "GIT_CLEAN", git?.clean?.let { "\"$it\"" }.toString())
        buildConfigField("String", "GIT_TAG", git?.tag?.let { "\"$it\"" }.toString())

        var version = (git?.tag?.removePrefix("v") ?: git?.commit?.substring(0, 10) ?: "unknown")
        git?.takeIf { !it.clean }?.let { version += "-dirty" }
        buildConfigField("String", "VERSION", "\"" + version + "\"")

        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
        packaging {
            // jniLibs.useLegacyPackaging = false
            // dex.useLegacyPackaging = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Debug: UniThen")

            buildFeatures {
                resValues = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        languageVersion.set(KotlinVersion.KOTLIN_2_4)
        freeCompilerArgs.add("-Xwarning-level=NOTHING_TO_INLINE:disabled")
        freeCompilerArgs.add("-Xintrinsic-const-evaluation")
        optIn.add("kotlin.time.ExperimentalTime")
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.compose.qr.code)

    implementation(libs.okhttp)
    implementation(libs.ksoup)

    implementation(libs.kotlinx.datetime)



    implementation(libs.kutil)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.compose)
    implementation(libs.androidx.camera.camera2)

    implementation(libs.kotlinx.serialization.json)


    testImplementation(libs.kotlin.test.core)
    testImplementation(libs.kotlin.test.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.zxingcpp)
}
