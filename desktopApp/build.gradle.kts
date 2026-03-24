import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvmToolchain(17)

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(project(":composeApp"))
                implementation("io.insert-koin:koin-core:3.5.3")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.salestrack.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KotlinMultiplatformComposeDesktopApplication"
            packageVersion = "1.0.0"
        }
    }
}

configurations.configureEach {
    resolutionStrategy {
        force("androidx.lifecycle:lifecycle-common:2.7.0")
        force("androidx.lifecycle:lifecycle-common-jvm:2.7.0")
        force("androidx.lifecycle:lifecycle-runtime:2.7.0")
        force("androidx.lifecycle:lifecycle-runtime-desktop:2.7.0")
        force("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
        force("androidx.lifecycle:lifecycle-viewmodel-desktop:2.7.0")
        force("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.7.0")
        force("androidx.lifecycle:lifecycle-viewmodel-savedstate-desktop:2.7.0")
    }
}
