package org.salestrack.app.core.di

import java.io.FileInputStream

class JvmFirebaseInitializer : FirebaseInitializer {
    override fun initialize() {
        try {
            // Usamos nombres de paquetes completos (FQDN) para saltarnos los conflictos de commonMain
            if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
                val path = "C:\\Users\\Hacker\\Desktop\\SENA\\app_movil\\salestrack.json"
                val serviceAccount = FileInputStream(path)

                // Al usar la ruta completa de FirebaseOptions, el builder aparecerá
                val options = com.google.firebase.FirebaseOptions.builder()
                    .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId("salestrack-d1a1a")
                    .setStorageBucket("salestrack-d1a1a.firebasestorage.app")
                    .build()

                // Esta función de Java NO pide el parámetro p0
                com.google.firebase.FirebaseApp.initializeApp(options)
                
                println("✅ Firebase Admin SDK (JVM) inicializado correctamente")
            }
        } catch (e: Exception) {
            println("❌ Error en JVM: ${e.message}")
            e.printStackTrace()
        }
    }
}

actual fun getFirebaseInitializer(): FirebaseInitializer = JvmFirebaseInitializer()