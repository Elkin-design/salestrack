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

    @Suppress("DEPRECATION")
    androidTarget()

    jvm()

    // Definición de targets de iOS
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0"
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
        pod("FirebaseCrashlytics")
        pod("FirebaseRemoteConfig")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
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
            }
        }

        // ✨ CAPA INTERMEDIA: Donde vive Firebase (Solo Android e iOS)
        val mobileMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.firebaseApp)
                implementation(libs.firebaseAuth)
                implementation(libs.firebaseFirestore)
                implementation(libs.firebaseCrashlytics)
                implementation(libs.firebaseConfig)
            }
        }

        val androidMain by getting {
            dependsOn(mobileMain)
            dependencies {
                implementation(libs.play.services.auth)
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain) // Desktop NO usa Firebase
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation(libs.skiko.awt.runtime.windows)
                implementation(libs.skiko.windows)
            }
        }

        // ✨ CORRECCIÓN PARA IOS EN WINDOWS:
        // Usamos 'creating' por si el plugin no los creó automáticamente
        val iosMain by creating {
            dependsOn(mobileMain)
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        // Conectamos los targets individuales a iosMain
        configure(listOf(iosX64Main, iosArm64Main, iosSimulatorArm64Main)) {
            dependsOn(iosMain)
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
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

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false // No ofusques aquí
            // Estas reglas se pasan automáticamente al módulo :android
            consumerProguardFiles("proguard-rules.pro")
        }
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