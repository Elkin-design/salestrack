package org.salestrack.app.domain.usecase.team

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.UserRole
import org.salestrack.app.domain.repository.TeamRepository

class InviteMemberUseCase(
    private val repository: TeamRepository,
) {
    suspend operator fun invoke(
        fullName: String,
        email: String,
        role: UserRole,
    ): AppResult<TeamMember> {
        if (fullName.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El nombre es obligatorio"))
        }
        if (!EMAIL_REGEX.matches(email.trim())) {
            return AppResult.Failure(IllegalArgumentException("Correo inválido"))
        }
        return repository.inviteMember(fullName.trim(), email.trim(), role)
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    }
}

