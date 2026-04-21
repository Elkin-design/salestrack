package org.salestrack.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.salestrack.app.App
import org.salestrack.app.core.di.setAndroidContext
import org.salestrack.app.core.utils.platformGoogleSignInNavigator
import org.salestrack.app.core.utils.AndroidGoogleSignInNavigator
import android.content.Intent

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (platformGoogleSignInNavigator as? AndroidGoogleSignInNavigator)?.handleActivityResult(requestCode, data)
    }
}
