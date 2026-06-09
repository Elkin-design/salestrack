package org.salestrack.app.core.utils

/**
 * Interface to trigger native Google Sign-In selection.
 * Implementation is platform-specific.
 */
interface GoogleSignInNavigator {
    fun signIn(onResult: (idToken: String?, error: String?) -> Unit)
    fun signOut(onComplete: () -> Unit = {})
}

/**
 * Default provider to be injected.
 */
expect val platformGoogleSignInNavigator: GoogleSignInNavigator
