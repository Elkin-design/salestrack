package org.salestrack.app.presentation.feature.team

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeDispatcherProvider
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.repository.FakeTeamRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.data.source.InMemoryTeamDataSource
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.UserRole
import org.salestrack.app.domain.usecase.team.GetRolePermissionsUseCase
import org.salestrack.app.domain.usecase.team.GetTeamSalesUseCase
import org.salestrack.app.domain.usecase.team.InviteMemberUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TeamViewModelTest {

    @Test
    fun should_load_team_ranking_for_admin() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(
                listOf(
                    Sale("1", "Cafe", "Bebidas", 1, 10_000.0, 0.0, 1_000L, "Ana"),
                ),
            ),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val teamRepository = FakeTeamRepository(
            InMemoryTeamDataSource(
                listOf(TeamMember("U-1", "Ana", "ana@test.com", UserRole.Admin)),
            ),
        )

        val viewModel = TeamViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            saleRepository = saleRepository,
            teamRepository = teamRepository,
            getTeamSalesUseCase = GetTeamSalesUseCase(),
            inviteMemberUseCase = InviteMemberUseCase(teamRepository),
            getRolePermissionsUseCase = GetRolePermissionsUseCase(),
        )

        advanceUntilIdle()

        assertEquals(UserRole.Admin, viewModel.state.value.currentRole)
        assertEquals(1, viewModel.state.value.ranking.size)
        assertTrue(viewModel.state.value.permissions.canManageTeam)
    }

    @Test
    fun should_block_invite_for_seller_role() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(emptyList()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val teamRepository = FakeTeamRepository(InMemoryTeamDataSource(emptyList()))

        val viewModel = TeamViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            saleRepository = saleRepository,
            teamRepository = teamRepository,
            getTeamSalesUseCase = GetTeamSalesUseCase(),
            inviteMemberUseCase = InviteMemberUseCase(teamRepository),
            getRolePermissionsUseCase = GetRolePermissionsUseCase(),
        )

        advanceUntilIdle()
        viewModel.onEvent(TeamUiEvent.ChangeCurrentRole(UserRole.Seller))
        viewModel.onEvent(TeamUiEvent.InviteMember("Carlos", "carlos@test.com", UserRole.Seller))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
        assertTrue(viewModel.state.value.permissions.canViewOnlyOwnSales)
    }

    @Test
    fun should_update_selected_member_sales_when_member_changes() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(
                listOf(
                    Sale("1", "Cafe", "Bebidas", 1, 10_000.0, 0.0, 1_000L, "Ana"),
                    Sale("2", "Galletas", "Snacks", 2, 5_000.0, 0.0, 2_000L, "Luis"),
                ),
            ),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val teamRepository = FakeTeamRepository(
            InMemoryTeamDataSource(
                listOf(
                    TeamMember("U-1", "Ana", "ana@test.com", UserRole.Admin),
                    TeamMember("U-2", "Luis", "luis@test.com", UserRole.Seller),
                ),
            ),
        )

        val viewModel = TeamViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            saleRepository = saleRepository,
            teamRepository = teamRepository,
            getTeamSalesUseCase = GetTeamSalesUseCase(),
            inviteMemberUseCase = InviteMemberUseCase(teamRepository),
            getRolePermissionsUseCase = GetRolePermissionsUseCase(),
        )

        advanceUntilIdle()

        viewModel.onEvent(TeamUiEvent.SelectMember("U-2"))
        advanceUntilIdle()

        assertEquals("U-2", viewModel.state.value.selectedMemberId)
        assertEquals(1, viewModel.state.value.selectedMemberSales.size)
        assertEquals("Luis", viewModel.state.value.selectedMemberSales.first().sellerName)
    }
}

