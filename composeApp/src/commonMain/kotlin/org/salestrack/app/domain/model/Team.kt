package org.salestrack.app.domain.model

enum class UserRole {
    Admin,
    Supervisor,
    Seller,
}

data class TeamMember(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean = true,
)

data class TeamMemberPerformance(
    val memberId: String,
    val memberName: String,
    val role: UserRole,
    val totalSold: Double,
    val transactionCount: Int,
    val averageTicket: Double,
)

data class RolePermissions(
    val canViewAllSales: Boolean,
    val canManageTeam: Boolean,
    val canViewOnlyOwnSales: Boolean,
)

