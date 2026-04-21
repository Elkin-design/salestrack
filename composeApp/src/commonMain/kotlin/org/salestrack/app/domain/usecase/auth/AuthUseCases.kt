package org.salestrack.app.domain.usecase.auth

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.repository.AuthRepository
import org.salestrack.app.domain.repository.AuthUser

class SignInWithGoogleUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): AppResult<AuthUser> {
        return repository.signInWithGoogle(idToken)
    }
}

class SignOutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return repository.signOut()
    }
}

class GetAuthStateUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.observeAuthState()
}
