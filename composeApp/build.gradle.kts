import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    kotlin("native.cocoapods")
}

kotlin {
    jvmToolchain(17)

androidTarget {
    compilations.all {
        compileTaskProvider.configure {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}

    cocoapods {
        summary = "SalesTrack Compose Multiplatform App"
        homepage = "https://github.com/JetBrains/compose-multiplatform"
        ios.deploymentTarget = "15.0"
        framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        pod("FirebaseCore")
        pod("FirebaseAuth")
        pod("FirebaseFirestore")
    }

    jvm()

    sourceSets {

        val androidMain by getting {
            dependencies {
                //implementation(platform(libs.firebase.bom))
            }
        }

        val commonMain by getting {
            dependencies {

                // Compose Multiplatform correcto (1.7+)
               implementation(compose.material3)
implementation(compose.foundation)
implementation(compose.ui)
implementation(compose.runtime)
implementation(compose.components.resources)
implementation(compose.components.uiToolingPreview)
implementation(compose.materialIconsExtended)

                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.firebaseCommon)
                implementation(libs.firebaseAuth)
                implementation(libs.firebaseFirestore)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                // Provides the pure-JVM Firebase implementation (no Android Context needed).
                // Without this, the Android-backed firebase-common is resolved on JVM,
                // causing "null cannot be cast to android.content.Context" at startup.
                implementation(libs.firebaseJavaSdk)
            }
        }
    }
}
android {
    namespace = "org.salestrack.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
compose.desktop {
    application {
        mainClass = "org.salestrack.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.salestrack.app"
            packageVersion = "1.0.0"
        }
    }
}