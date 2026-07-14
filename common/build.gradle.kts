import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization) version "2.4.0"
}

kotlin {
    jvmToolchain(11)

    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_4)
        freeCompilerArgs.add("-Xwarning-level=NOTHING_TO_INLINE:disabled")
        freeCompilerArgs.add("-Xintrinsic-const-evaluation")
    }

    androidLibrary {
        namespace = "de.bixilon.unithen.common"
        compileSdk = 36
        minSdk = 26
        androidResources.enable = true
    }

    sourceSets {

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


            implementation(libs.okhttp)
            implementation(libs.ksoup)
            implementation(libs.kutil)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.ui)
            implementation(libs.androidx.ui.graphics)
            implementation(libs.androidx.material3)

            implementation(libs.zxing)
            implementation(libs.zxingcpp)


            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.preferences)

            // TODO: exclude appcompat
            implementation(libs.androidx.camera.core) // { exclude("androidx.appcompat", "appcompat") }
            implementation(libs.androidx.camera.view) // { exclude("androidx.appcompat", "appcompat") }
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.compose)
            implementation(libs.androidx.camera.camera2)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kutil)
        }
    }
}
