package org.salestrack.app.presentation.app.di

import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

import org.salestrack.app.core.di.initializeFirebase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

fun startAppKoinIfNeeded(config: EnvironmentConfig = EnvironmentConfig()) {
    if (GlobalContext.getOrNull() != null) return

    initializeFirebase()
    
    // Test Firebase connection
    try {
        val auth = Firebase.auth
        println("🔥 Firebase: Instancia obtenida con éxito. Usuario actual: ${auth.currentUser}")
    } catch (e: Exception) {
        println("❌ Firebase: Error de inicialización: ${e.message}")
    }

    startKoin {
        modules(appModule(config))
    }
}
