package org.salestrack.app.core.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.initialize
import java.io.File

/**
 * Inicializador de Firebase para JVM (Compose Desktop).
 *
 * En JVM el GitLive SDK usa firebase-java-sdk internamente (puerto del SDK Android),
 * por lo que necesitas:
 *   1. Inicializar FirebasePlatform (manejo de logs y almacenamiento de tokens)
 *   2. Llamar Firebase.initialize(options = FirebaseOptions(...)) con datos de cliente
 *
 * NOTA: El Service Account JSON es para Firebase Admin SDK (backend).
 *       Para Compose Desktop se usan las credenciales de cliente (API key, App ID, Project ID).
 */
class JvmFirebaseInitializer : FirebaseInitializer {
    override fun initialize() {
        try {
            setupFirebasePlatform()
            initFirebaseApp()
            println("✅ Firebase (JVM) inicializado correctamente")
        } catch (e: Exception) {
            println("❌ Firebase (JVM): Error en inicialización: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Implementación de FirebasePlatform obligatoria para JVM.
     * Provee al SDK de logging y almacenamiento persistente (tokens de Auth, etc.).
     */
    private fun setupFirebasePlatform() {
        // Directorio para persistir datos de Auth (tokens, sesión)
        val storageDir = File(System.getProperty("user.home"), ".salestrack/firebase")
        storageDir.mkdirs()
        val storageFile = File(storageDir, "auth_storage.properties")

        val properties = java.util.Properties().apply {
            if (storageFile.exists()) load(storageFile.inputStream())
        }

        FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
            override fun store(key: String, value: String) {
                properties.setProperty(key, value)
                storageFile.outputStream().use { properties.store(it, null) }
            }

            override fun retrieve(key: String): String? = properties.getProperty(key)

            override fun clear(key: String) {
                properties.remove(key)
                storageFile.outputStream().use { properties.store(it, null) }
            }

            override fun log(msg: String) = println("[Firebase JVM] $msg")

            override fun getDatabasePath(name: String): File =
                File(storageDir, name)
        })
    }

    /**
     * Inicializa la app Firebase con las credenciales de cliente.
     * Estos valores provienen de google-services.json / Firebase Console.
     */
    private fun initFirebaseApp() {
        val options = FirebaseOptions(
            // Desde androidApp/google-services.json → api_key
            apiKey = "AIzaSyAsBQmiPA5g62bje_BBRQSqFJRKjR6XK8g",
            // Desde androidApp/google-services.json → mobilesdk_app_id
            applicationId = "1:402319710438:android:2d1c49ba433d86055e8327",
            // Desde androidApp/google-services.json → project_id
            projectId = "salestrack-d1a1a",
            // Desde androidApp/google-services.json → storage_bucket
            storageBucket = "salestrack-d1a1a.firebasestorage.app",
            // Desde androidApp/google-services.json → project_number
            gcmSenderId = "402319710438",
            // Auth domain estándar de Firebase
            authDomain = "salestrack-d1a1a.firebaseapp.com"
        )
        Firebase.initialize(options = options)
    }
}

actual fun getFirebaseInitializer(): FirebaseInitializer = JvmFirebaseInitializer()