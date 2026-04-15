package org.salestrack.app.core.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.firestore

object FirebaseHelpers {

    /**
     * Devuelve una referencia al documento del usuario actual.
     * Si no hay usuario autenticado, utiliza "default_user" mientras llega la implementación de Auth.
     */
    fun userRootDocument(): DocumentReference {
        val currentUser = Firebase.auth.currentUser
        val uid = currentUser?.uid ?: "default_user_1"
        return Firebase.firestore.collection("users").document(uid)
    }
}
