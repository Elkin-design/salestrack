package org.salestrack.app.core.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.initialize
import java.io.File

class MyFirebasePlatform : FirebasePlatform() {
    val storage = mutableMapOf<String, String>()
    override fun store(key: String, value: String) { storage[key] = value }
    override fun retrieve(key: String): String? = storage[key]
    override fun clear(key: String) { storage.remove(key) }
    override fun log(msg: String) = println("Firebase: $msg")
    override fun getDatabasePath(name: String): File =
        File("${System.getProperty("java.io.tmpdir")}${File.separatorChar}$name")
}

class JvmFirebaseInitializer : FirebaseInitializer {
    override fun initialize() {
        try {
            // Step 1: Register the JVM FirebasePlatform for storage/logging.
            FirebasePlatform.initializeFirebasePlatform(MyFirebasePlatform())

            // Step 2: Initialize Firebase using the gitlive firebase-java-sdk overload that takes
            // NO context. The firebase-java-sdk (dev.gitlive:firebase-java-sdk) is a pure JVM
            // port — unlike the Android SDK, it does not need an android.content.Context.
            Firebase.initialize(
                options = FirebaseOptions(
                    apiKey = "AIzaSyAsBQmiPA5g62bje_BBRQSqFJRKjR6XK8g",
                    applicationId = "1:402319710438:android:2d1c49ba433d86055e8327",
                    projectId = "salestrack-d1a1a",
                    storageBucket = "salestrack-d1a1a.firebasestorage.app",
                    gcmSenderId = "402319710438"
                )
            )

            println("✅ Firebase initialized successfully on JVM")
        } catch (e: Throwable) {
            println("❌ Error initializing Firebase on JVM: ${e.message}")
            e.printStackTrace()
        }
    }
}

actual fun getFirebaseInitializer(): FirebaseInitializer = JvmFirebaseInitializer()
