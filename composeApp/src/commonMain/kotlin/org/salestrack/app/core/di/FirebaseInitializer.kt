package org.salestrack.app.core.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

/**
 * Common entry point for Firebase initialization across platforms.
 */
interface FirebaseInitializer {
    fun initialize()
}

/**
 * Expected function to be implemented by each platform.
 */
expect fun getFirebaseInitializer(): FirebaseInitializer

/**
 * Helper to initialize Firebase in the shared code if needed.
 */
fun initializeFirebase() {
    getFirebaseInitializer().initialize()
}
