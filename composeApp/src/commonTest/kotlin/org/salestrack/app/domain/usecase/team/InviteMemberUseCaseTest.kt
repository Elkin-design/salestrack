package org.salestrack.app.domain.usecase.team

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.repository.FakeTeamRepository
import org.salestrack.app.data.source.InMemoryTeamDataSource
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.UserRole
import kotlin.test.Test
import kotlin.test.assertTrue

class InviteMemberUseCaseTest {

    @Test
    fun should_invite_member_when_data_is_valid() = runTest {
        val repository = FakeTeamRepository(InMemoryTeamDataSource(emptyList()))
        val useCase = InviteMemberUseCase(repository)

        val result = useCase("Carlos", "carlos@test.com", UserRole.Seller)

        assertTrue(result is AppResult.Success)
    }

    @Test
    fun should_fail_when_email_is_invalid() = runTest {
        val repository = FakeTeamRepository(InMemoryTeamDataSource(emptyList()))
        val useCase = InviteMemberUseCase(repository)

        val result = useCase("Carlos", "correo-invalido", UserRole.Seller)

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun should_fail_when_email_exists() = runTest {
        val repository = FakeTeamRepository(
            InMemoryTeamDataSource(
                listOf(TeamMember("U-1", "Carlos", "carlos@test.com", UserRole.Seller)),
            ),
        )
        val useCase = InviteMemberUseCase(repository)

        val result = useCase("Carlos 2", "carlos@test.com", UserRole.Supervisor)

        assertTrue(result is AppResult.Failure)
    }
}

