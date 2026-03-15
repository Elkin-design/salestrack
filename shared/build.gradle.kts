plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget()
    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                
                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${project.property("kotlinx.datetime.version")}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${project.property("coroutines.version")}")

                // Koin
                implementation("io.insert-koin:koin-core:${project.property("koin.version")}")
                implementation("io.insert-koin:koin-compose:${project.property("koin.compose.version")}")
                
                // SQLDelight
                implementation("app.cash.sqldelight:coroutines-extensions:${project.property("sqldelight.version")}")
                implementation("app.cash.sqldelight:primitive-adapters:${project.property("sqldelight.version")}")
                
                // Ktor
                implementation("io.ktor:ktor-client-core:${project.property("ktor.version")}")
                implementation("io.ktor:ktor-client-content-negotiation:${project.property("ktor.version")}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${project.property("ktor.version")}")

                // Firebase (using GitLive for KMP)
                implementation("dev.gitlive:firebase-auth:${project.property("firebase.kmp.version")}")
                implementation("dev.gitlive:firebase-firestore:${project.property("firebase.kmp.version")}")

                // Apache POI for Excel
                implementation("org.apache.poi:poi-ooxml:${project.property("poi.version")}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${project.property("coroutines.version")}")
                implementation("io.insert-koin:koin-test:${project.property("koin.version")}")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.1")
                
                implementation("app.cash.sqldelight:android-driver:${project.property("sqldelight.version")}")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("io.mockk:mockk:${project.property("mockk.version")}")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation("app.cash.sqldelight:native-driver:${project.property("sqldelight.version")}")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation("app.cash.sqldelight:sqlite-driver:${project.property("sqldelight.version")}")
                // PDF for Desktop
                implementation("com.github.librepdf:openpdf:1.3.30")
            }
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

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.salestrack.app"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
