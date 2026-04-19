package org.salestrack.app.core.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

/**
 * Proveedor centralizado de instancias Firebase para uso en commonMain.
 * Funciona en Android, iOS y JVM sin ningún código específico de plataforma.
 * IMPORTANTE: Firebase debe ser inicializado antes de acceder a estas instancias.
 */
object FirebaseProvider {
    val auth by lazy { Firebase.auth }
    val firestore by lazy { Firebase.firestore }
}
