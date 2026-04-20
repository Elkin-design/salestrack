package org.salestrack.app.core.di

import android.content.Context
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

/**
 * Inicializador de Firebase para Android.
 * Usa Firebase.initialize(context) del SDK de GitLive, que a su vez
 * inicializa el SDK nativo de Android usando google-services.json.
 */
class AndroidFirebaseInitializer(private val context: Context) : FirebaseInitializer {
    override fun initialize() {
        Firebase.initialize(context)
        println("✅ Firebase (Android) inicializado correctamente")
    }
}

// Estado interno para guardar el contexto antes de la inicialización
internal var androidContextStore: Context? = null

/**
 * Debe llamarse desde MainActivity/Application con el Context de Android
 * ANTES de que se llame a initializeFirebase() desde commonMain.
 */
fun setAndroidContext(context: Context) {
    androidContextStore = context
}

actual fun getFirebaseInitializer(): FirebaseInitializer {
    val ctx = androidContextStore
        ?: throw IllegalStateException(
            "❌ Android: llama a setAndroidContext(context) en MainActivity antes de inicializar Firebase."
        )
    return AndroidFirebaseInitializer(ctx)
}
