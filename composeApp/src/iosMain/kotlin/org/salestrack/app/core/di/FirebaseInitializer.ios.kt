package org.salestrack.app.core.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class IosFirebaseInitializer : FirebaseInitializer {
    override fun initialize() {
        // On iOS, we need to ensure FirebaseApp.configure() is called.
        // The GitLive SDK handles this when you call Firebase.initialize()
        Firebase.initialize()
    }
}

actual fun getFirebaseInitializer(): FirebaseInitializer = IosFirebaseInitializer()
