package org.salestrack.app.core.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

class JvmFirebaseInitializer : FirebaseInitializer {
    override fun initialize() {
        try {
            // Initialize Firebase using gitlive firebase-app for Desktop/JVM.
            // It uses FirebaseOptions and does not need an Android Context.
            Firebase.initialize(
                options = FirebaseOptions(
                    applicationId = "1:402319710438:android:2d1c49ba433d86055e8327",
                    apiKey = "AIzaSyAsBQmiPA5g62bje_BBRQSqFJRKjR6XK8g",
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

actual fun getFirebaseInitializer(): FirebaseInitializer = JvmFirebaseInitializer()
