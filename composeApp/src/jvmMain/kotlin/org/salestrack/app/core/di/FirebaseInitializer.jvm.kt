package org.salestrack.app.core.di

class JvmFirebaseInitializer : FirebaseInitializer {
    override fun initialize() {
        // Firebase GitLive SDK doesn't support JVM/Desktop directly.
        // For Desktop, we generally use the Admin SDK or keep using the Mock/Stub backend.
    }
}

actual fun getFirebaseInitializer(): FirebaseInitializer = JvmFirebaseInitializer()
