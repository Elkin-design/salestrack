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
                // Firebase se inicializa via Firebase.initialize(context)
                // Los SDKs de GitLive están en commonMain; aquí solo van deps Android-específicas.
            }
        }

        val commonMain by getting {
            dependencies {
                // Firebase GitLive SDK — misma API para Android, iOS y JVM
                implementation(libs.firebaseApp)
                implementation(libs.firebaseAuth)
                implementation(libs.firebaseFirestore)

                // Compose Multiplatform
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
                implementation(libs.skiko.awt.runtime.windows)
                implementation(libs.skiko.windows)
                // firebase-admin no es necesario: usamos GitLive SDK (firebase-kotlin-sdk)
                // que internamente usa firebase-java-sdk como cliente — no Admin SDK.
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