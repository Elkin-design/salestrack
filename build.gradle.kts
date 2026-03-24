plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.googleServices) apply false
}

// Convenience tasks for building Android app
tasks.register("buildAndroidDebug") {
    dependsOn(":composeApp:assembleDebug")
    doLast {
        println("✓ Android Debug APK built successfully!")
        println("Location: composeApp/build/outputs/apk/debug/composeApp-debug.apk")
    }
}

tasks.register("buildAndroidRelease") {
    dependsOn(":composeApp:assembleRelease")
    doLast {
        println("✓ Android Release APK built successfully!")
        println("Location: composeApp/build/outputs/apk/release/composeApp-release.apk")
    }
}


allprojects {
    dependencies {
        modules {
            module("androidx.lifecycle:lifecycle-common") {
                replacedBy("org.jetbrains.androidx.lifecycle:lifecycle-common", "Replaced by Compose Multiplatform")
            }
            module("androidx.lifecycle:lifecycle-runtime") {
                replacedBy("org.jetbrains.androidx.lifecycle:lifecycle-runtime", "Replaced by Compose Multiplatform")
            }
            module("androidx.lifecycle:lifecycle-viewmodel") {
                replacedBy("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel", "Replaced by Compose Multiplatform")
            }
            module("androidx.lifecycle:lifecycle-viewmodel-savedstate") {
                replacedBy("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-savedstate", "Replaced by Compose Multiplatform")
            }
        }
    }
}
