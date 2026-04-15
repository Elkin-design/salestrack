package org.salestrack.app.core.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class AndroidFirebaseInitializer : FirebaseInitializer {
    override fun initialize() {
        // On Android, the google-services plugin handles the native initialization
        // but we can ensure the GitLive wrapper is ready.
        // If needed, we can pass Context here, but typically Firebase.initialize(context)
        // is called by the plugin automatically.
    }
}

actual fun getFirebaseInitializer(): FirebaseInitializer = AndroidFirebaseInitializer()
