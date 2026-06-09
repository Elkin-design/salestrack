package org.salestrack.app.domain.usecase.auth

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.repository.AuthRepository
import org.salestrack.app.domain.repository.AuthUser

import org.salestrack.app.core.utils.GoogleSignInNavigator
import org.salestrack.app.core.utils.platformGoogleSignInNavigator

class SignInWithGoogleUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AppResult<AuthUser> {
        return repository.signInWithGoogle(idToken)
    }
}

class SignOutUseCase(
    private val repository: AuthRepository,
    private val googleSignInNavigator: GoogleSignInNavigator = platformGoogleSignInNavigator
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val repoResult = repository.signOut()
        if (repoResult is AppResult.Success) {
            // Se cierra la sesión en el cliente nativo de Google para limpiar el token
            // y que el selector de cuentas vuelva a aparecer.
            // Para simplificar la suspensión, llamamos a signOut de manera asíncrona.
            googleSignInNavigator.signOut()
        }
        return repoResult
    }
}

class GetAuthStateUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.observeAuthState()
}
