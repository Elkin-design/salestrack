package org.salestrack.app.core.utils

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.salestrack.app.core.di.androidContextStore

class AndroidGoogleSignInNavigator : GoogleSignInNavigator {
    
    private var onResultCallback: ((String?, String?) -> Unit)? = null

    override fun signIn(onResult: (idToken: String?, error: String?) -> Unit) {
        val activity = androidContextStore as? Activity ?: run {
            onResult(null, "No se pudo encontrar una actividad válida")
            return
        }
        
        onResultCallback = onResult

        // NOTA: El Web Client ID debería estar en google-services.json o en tus ajustes
        // Si no está, Google Sign-In fallará. Aquí usamos un placeholder o intentamos
        // que el usuario lo configure.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("104036343928293051753") // TODO: El usuario debe poner su Web Client ID real aquí
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        
        // Aquí hay un reto: Cómo capturar el resultado sin modificar MainActivity
        // Una opción es que MainActivity pase el resultado aquí, o usar un fragmento invisible.
        // Para este MVP, asumiremos que el usuario integrará la captura del resultado.
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun signOut(onComplete: () -> Unit) {
        val activity = androidContextStore as? Activity ?: run {
            onComplete()
            return
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("104036343928293051753")
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    fun handleActivityResult(requestCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                onResultCallback?.invoke(account.idToken, null)
            } catch (e: ApiException) {
                onResultCallback?.invoke(null, "Error de Google Sign-In: ${e.statusCode}")
            }
        }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}

actual val platformGoogleSignInNavigator: GoogleSignInNavigator = AndroidGoogleSignInNavigator()
