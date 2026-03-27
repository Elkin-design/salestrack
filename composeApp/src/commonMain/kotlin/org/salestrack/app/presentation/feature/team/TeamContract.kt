package org.salestrack.app.presentation.feature.team

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.RolePermissions
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.TeamMemberPerformance
import org.salestrack.app.domain.model.UserRole

data class TeamUiState(
    val isLoading: Boolean = true,
    val currentRole: UserRole = UserRole.Admin,
    val selectedCategory: String? = null,
    val permissions: RolePermissions = RolePermissions(
        canViewAllSales = true,
        canManageTeam = true,
        canViewOnlyOwnSales = false,
    ),
    val categories: List<String> = emptyList(),
    val members: List<TeamMember> = emptyList(),
    val ranking: List<TeamMemberPerformance> = emptyList(),
    val selectedMemberId: String? = null,
    val selectedMemberSales: List<Sale> = emptyList(),
    val errorMessage: String? = null,
) : UiState

sealed interface TeamUiEvent : UiEvent {
    data object Refresh : TeamUiEvent
    data class ChangeCurrentRole(val role: UserRole) : TeamUiEvent
    data class ChangeCategory(val category: String?) : TeamUiEvent
    data class SelectMember(val memberId: String?) : TeamUiEvent
    data class InviteMember(val fullName: String, val email: String, val role: UserRole) : TeamUiEvent
    data class ChangeMemberRole(val memberId: String, val role: UserRole) : TeamUiEvent
    data class RemoveMember(val memberId: String) : TeamUiEvent
}

sealed interface TeamUiEffect : UiEffect {
    data class ShowMessage(val message: String) : TeamUiEffect
}

