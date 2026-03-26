package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.source.InMemoryTeamDataSource
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.UserRole
import org.salestrack.app.domain.repository.TeamRepository

class FakeTeamRepository(
    private val dataSource: InMemoryTeamDataSource,
) : TeamRepository {

    override fun observeMembers(): Flow<List<TeamMember>> = dataSource.observe()

    override suspend fun inviteMember(
        fullName: String,
        email: String,
        role: UserRole,
    ): AppResult<TeamMember> {
        if (dataSource.getCurrent().any { it.email.equals(email, ignoreCase = true) }) {
            return AppResult.Failure(IllegalStateException("El correo ya pertenece al equipo"))
        }

        val member = TeamMember(
            id = "U-${dataSource.getCurrent().size + 1}",
            fullName = fullName,
            email = email,
            role = role,
        )
        dataSource.replaceAll(dataSource.getCurrent() + member)
        return AppResult.Success(member)
    }

    override suspend fun updateMemberRole(memberId: String, role: UserRole): AppResult<TeamMember> {
        val current = dataSource.getCurrent()
        val index = current.indexOfFirst { it.id == memberId }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Miembro no encontrado"))
        }

        val updatedMember = current[index].copy(role = role)
        val updated = current.toMutableList().apply { set(index, updatedMember) }
        dataSource.replaceAll(updated)
        return AppResult.Success(updatedMember)
    }

    override suspend fun removeMember(memberId: String): AppResult<Unit> {
        val current = dataSource.getCurrent()
        if (current.none { it.id == memberId }) {
            return AppResult.Failure(NoSuchElementException("Miembro no encontrado"))
        }
        dataSource.replaceAll(current.filterNot { it.id == memberId })
        return AppResult.Success(Unit)
    }
}

