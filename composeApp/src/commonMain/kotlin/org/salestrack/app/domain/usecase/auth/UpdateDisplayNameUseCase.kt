package org.salestrack.app.domain.usecase.auth

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.repository.AuthRepository

class UpdateDisplayNameUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String): AppResult<Unit> {
        return authRepository.updateDisplayName(name)
    }
}
