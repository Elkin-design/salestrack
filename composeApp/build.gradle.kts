import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("app.cash.sqldelight")
    id("com.google.gms.google-services")
}

kotlin {
    jvmToolchain(17)

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("app.cash.sqldelight:android-driver:2.0.1")
            implementation("com.google.mlkit:barcode-scanning:17.0.3")
            implementation("androidx.camera:camera-camera2:1.3.0")
            implementation("androidx.camera:camera-lifecycle:1.3.0")
            implementation("androidx.camera:camera-view:1.3.0")
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            // Lifecycle for Multiplatform
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
            
            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

            // Koin
            implementation("io.insert-koin:koin-core:3.5.3")
            implementation("io.insert-koin:koin-compose:1.1.2")
            
            // SQLDelight
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
            implementation("app.cash.sqldelight:primitive-adapters:2.0.1")
            
            // Firebase
            implementation("dev.gitlive:firebase-auth:1.11.1") {
                exclude("androidx.lifecycle")
                exclude("android.arch.lifecycle")
            }
            implementation("dev.gitlive:firebase-firestore:1.11.1") {
                exclude("androidx.lifecycle")
                exclude("android.arch.lifecycle")
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.mockk:mockk:1.13.8")
            }
        }
        androidMain.dependencies {
            implementation("app.cash.sqldelight:android-driver:2.0.1")
            implementation("org.apache.poi:poi-ooxml:5.2.5")
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
            implementation("com.github.librepdf:openpdf:1.3.30")
            implementation("org.apache.poi:poi-ooxml:5.2.5")
        }
    }
}

android {
    namespace = "org.salestrack.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.salestrack.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        abortOnError = false
        checkDependencies = false
        disable += setOf("InvalidPackage", "UnusedResources")
        checkTestSources = false
        ignoreTestSources = true
    }

    configurations.configureEach {
        if (name.contains("AndroidTest", ignoreCase = true)) {
            resolutionStrategy {
                force("com.google.firebase:firebase-auth-ktx:22.3.0")
                force("com.google.firebase:firebase-firestore:24.10.0")
                force("com.google.firebase:firebase-common-ktx:20.4.2")
            }
        }
        
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.jetbrains.androidx.lifecycle:lifecycle-common")).using(module("androidx.lifecycle:lifecycle-common:2.8.5"))
            substitute(module("org.jetbrains.androidx.lifecycle:lifecycle-runtime")).using(module("androidx.lifecycle:lifecycle-runtime:2.8.5"))
            substitute(module("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel")).using(module("androidx.lifecycle:lifecycle-viewmodel:2.8.5"))
        }
    }
}

sqldelight {
    databases {
        create("SalesTrackDatabase") {
            packageName.set("com.salestrack.db")
        }
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
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
