package org.salestrack.app.core.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

/**
 * Inicializador de Firebase para iOS.
 * Firebase.initialize() es suficiente en iOS porque el SDK nativo
 * se configura automáticamente leyendo el GoogleService-Info.plist
 * que el compilador embebe en el bundle.
 *
 * NOTA: También puedes llamar Firebase.initialize() desde el AppDelegate
 *       de Swift antes de que Kotlin entre en juego.
 */
class IosFirebaseInitializer : FirebaseInitializer {
    override fun initialize() {
        Firebase.initialize()
        println("✅ Firebase (iOS) inicializado correctamente")
    }
}

actual fun getFirebaseInitializer(): FirebaseInitializer = IosFirebaseInitializer()
