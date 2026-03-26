package org.salestrack.app.presentation.feature.team

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.UserRole
import org.salestrack.app.domain.repository.SaleRepository
import org.salestrack.app.domain.repository.TeamRepository
import org.salestrack.app.domain.usecase.team.GetRolePermissionsUseCase
import org.salestrack.app.domain.usecase.team.GetTeamSalesUseCase
import org.salestrack.app.domain.usecase.team.InviteMemberUseCase

class TeamViewModel(
    dispatcherProvider: DispatcherProvider,
    private val saleRepository: SaleRepository,
    private val teamRepository: TeamRepository,
    private val getTeamSalesUseCase: GetTeamSalesUseCase,
    private val inviteMemberUseCase: InviteMemberUseCase,
    private val getRolePermissionsUseCase: GetRolePermissionsUseCase,
) : BaseViewModel<TeamUiState, TeamUiEvent, TeamUiEffect>(
    initialState = TeamUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    private var latestSales: List<Sale> = emptyList()
    private var latestMembers: List<TeamMember> = emptyList()

    init {
        observeSales()
        observeMembers()
    }

    override fun onEvent(event: TeamUiEvent) {
        when (event) {
            TeamUiEvent.Refresh -> refreshRanking()
            is TeamUiEvent.ChangeCurrentRole -> {
                setState { it.copy(currentRole = event.role) }
                refreshRanking()
            }
            is TeamUiEvent.ChangeCategory -> {
                setState { it.copy(selectedCategory = event.category) }
                refreshRanking()
            }
            is TeamUiEvent.SelectMember -> setState { it.copy(selectedMemberId = event.memberId) }
            is TeamUiEvent.InviteMember -> inviteMember(event.fullName, event.email, event.role)
            is TeamUiEvent.ChangeMemberRole -> changeMemberRole(event.memberId, event.role)
            is TeamUiEvent.RemoveMember -> removeMember(event.memberId)
        }
    }

    private fun observeSales() {
        scope.launch {
            saleRepository.observeSales().collect { sales ->
                latestSales = sales
                updateCategories()
                refreshRanking()
            }
        }
    }

    private fun observeMembers() {
        scope.launch {
            teamRepository.observeMembers().collect { members ->
                latestMembers = members
                refreshRanking()
            }
        }
    }

    private fun updateCategories() {
        val categories = latestSales.map { it.category }.distinct().sorted()
        setState { it.copy(categories = categories) }
    }

    private fun refreshRanking() {
        val current = state.value
        val permissions = getRolePermissionsUseCase(current.currentRole)
        val ranking = getTeamSalesUseCase(
            sales = latestSales,
            members = latestMembers,
            category = current.selectedCategory,
        )

        val visibleRanking = if (permissions.canViewOnlyOwnSales) {
            ranking.take(1)
        } else {
            ranking
        }

        setState {
            it.copy(
                isLoading = false,
                members = latestMembers,
                ranking = visibleRanking,
                errorMessage = null,
            )
        }
    }

    private fun inviteMember(fullName: String, email: String, role: UserRole) {
        scope.launch {
            if (!getRolePermissionsUseCase(state.value.currentRole).canManageTeam) {
                setState { it.copy(errorMessage = "No tienes permisos para gestionar equipo") }
                return@launch
            }

            when (val result = inviteMemberUseCase(fullName, email, role)) {
                is AppResult.Success -> emitEffect(TeamUiEffect.ShowMessage("Miembro invitado"))
                is AppResult.Failure -> setState {
                    it.copy(errorMessage = result.error.message ?: "Error al invitar miembro")
                }
            }
        }
    }

    private fun changeMemberRole(memberId: String, role: UserRole) {
        scope.launch {
            if (!getRolePermissionsUseCase(state.value.currentRole).canManageTeam) {
                setState { it.copy(errorMessage = "No tienes permisos para gestionar equipo") }
                return@launch
            }
            when (val result = teamRepository.updateMemberRole(memberId, role)) {
                is AppResult.Success -> emitEffect(TeamUiEffect.ShowMessage("Rol actualizado"))
                is AppResult.Failure -> setState {
                    it.copy(errorMessage = result.error.message ?: "Error actualizando rol")
                }
            }
        }
    }

    private fun removeMember(memberId: String) {
        scope.launch {
            if (!getRolePermissionsUseCase(state.value.currentRole).canManageTeam) {
                setState { it.copy(errorMessage = "No tienes permisos para gestionar equipo") }
                return@launch
            }
            when (val result = teamRepository.removeMember(memberId)) {
                is AppResult.Success -> emitEffect(TeamUiEffect.ShowMessage("Miembro removido"))
                is AppResult.Failure -> setState {
                    it.copy(errorMessage = result.error.message ?: "Error removiendo miembro")
                }
            }
        }
    }
}


