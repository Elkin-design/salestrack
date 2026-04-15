package org.salestrack.app.core.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.FirebasePlatform
import java.io.File

class JvmFirebaseInitializer : FirebaseInitializer {
    override fun initialize() {
        try {
            // Initialize platform services (logging, storage path)
            FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
                val storage = mutableMapOf<String, String>()
                override fun store(key: String, value: String) { storage[key] = value }
                override fun retrieve(key: String): String? = storage[key]
                override fun clear(key: String) { storage.remove(key) }
                override fun log(msg: String) = println("Firebase: $msg")
                override fun getDatabasePath(name: String): File = 
                    File("${System.getProperty("java.io.tmpdir")}${File.separatorChar}$name")
            })

            // Using credentials from google-services.json
            val options = FirebaseOptions(
                apiKey = "AIzaSyAsBQmiPA5g62bje_BBRQSqFJRKjR6XK8g",
                applicationId = "1:402319710438:android:2d1c49ba433d86055e8327",
                projectId = "salestrack-d1a1a",
                storageBucket = "salestrack-d1a1a.firebasestorage.app",
                gcmSenderId = "402319710438"
            )

            Firebase.initialize(options = options)
            println("✅ Firebase initialized successfully on JVM")
        } catch (e: Exception) {
            println("❌ Error initializing Firebase on JVM: ${e.message}")
            // Don't rethrow, let the app try to continue or show its own error UI
        }
    }
}

actual fun getFirebaseInitializer(): FirebaseInitializer = JvmFirebaseInitializer()
