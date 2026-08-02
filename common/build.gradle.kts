import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val generatedSources = layout.buildDirectory.dir("generated/info/commonMain/kotlin")

val generateInfo = tasks.register("generateInfo") {
    val outputFile = generatedSources.map { it.file("de/bixilon/unithen/BuildInfo.kt") }

    outputs.file(outputFile)

    doLast {

        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(
                """
package de.bixilon.unithen

object BuildInfo {
    val VERSION: String get() = "${project.extra.get("version")}"
    val VERSION_CODE: Int get() = ${project.extra.get("versionCode")}
    val GIT_COMMIT: String get() = "${project.extra.get("commit")}"
}
"""
            )
        }
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir(generatedSources)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateInfo)
}

kotlin {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_4)
        freeCompilerArgs.add("-Xwarning-level=NOTHING_TO_INLINE:disabled")
        freeCompilerArgs.add("-Xintrinsic-const-evaluation")
    }

    android {
        namespace = "de.bixilon.unithen.common"
        compileSdk = 37
        //noinspection WrongGradleMethod
        val deviceTest = gradle.startParameter.taskNames.any { "android" in it.lowercase() && "test" in it.lowercase() }
        minSdk = if (deviceTest) 30 else 26
        androidResources.enable = true

        withDeviceTest { animationsDisabled = true }
    }

    jvm("jvm") {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }


    iosSimulatorArm64()
    iosArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val androidJvm = create("androidJvm") {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.zxing)
                implementation(libs.ktor.client.cio)
            }
        }


        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.navigationevent)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)



            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.material.icons.extended)

            implementation(libs.ktor.client.core)
            implementation(libs.ksoup)
            implementation(libs.kutil)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ui.test)

            implementation(libs.coil.svg)
            implementation(libs.coil.compose)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)

            implementation(libs.zxing)
            implementation(libs.zxingcpp)


            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.preferences)

            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.compose)
            implementation(libs.androidx.camera.camera2)

            configurations.configureEach {
                exclude("androidx.appcompat", "appcompat")
            }
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }

        named(androidMain.name) {
            dependsOn(androidJvm)
        }

        named(jvmMain.name) {
            dependsOn(androidJvm)
            dependencies {
                implementation(libs.sqlite.jdbc)
            }
        }

        named("androidDeviceTest") {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.androidx.runner)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":debug"))
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.coroutines.test)
        }

        nativeMain.dependencies {
            implementation(libs.sqliter.driver)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

// https://github.com/touchlab/SQLiter/issues/77
project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
    targets
        .filterIsInstance<KotlinNativeTarget>()
        .flatMap { it.binaries }
        .forEach { compilationUnit -> compilationUnit.linkerOpts("-lsqlite3") }
}

tasks.withType<Test> {
    maxParallelForks = maxOf(Runtime.getRuntime().availableProcessors() - 2, 1)
}
