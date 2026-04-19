package org.salestrack.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.salestrack.app.App
import org.salestrack.app.core.di.setAndroidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // ✅ PASO 1: Pasar el Context a la capa compartida ANTES de inicializar Firebase
        // Esto es obligatorio para que Firebase.initialize(context) funcione en Android.
        setAndroidContext(this)

        setContent {
            App()
        }
    }
}
