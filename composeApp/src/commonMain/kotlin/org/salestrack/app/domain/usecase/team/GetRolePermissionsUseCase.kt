package org.salestrack.app.domain.usecase.team

import org.salestrack.app.domain.model.RolePermissions
import org.salestrack.app.domain.model.UserRole

class GetRolePermissionsUseCase {
    operator fun invoke(role: UserRole): RolePermissions = when (role) {
        UserRole.Admin -> RolePermissions(
            canViewAllSales = true,
            canManageTeam = true,
            canViewOnlyOwnSales = false,
        )
        UserRole.Supervisor -> RolePermissions(
            canViewAllSales = true,
            canManageTeam = false,
            canViewOnlyOwnSales = false,
        )
        UserRole.Seller -> RolePermissions(
            canViewAllSales = false,
            canManageTeam = false,
            canViewOnlyOwnSales = true,
        )
    }
}

