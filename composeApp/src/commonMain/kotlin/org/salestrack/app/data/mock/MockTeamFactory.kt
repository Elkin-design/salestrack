package org.salestrack.app.data.mock

import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.UserRole

object MockTeamFactory {
    fun create(): List<TeamMember> = listOf(
        TeamMember(
            id = "U-001",
            fullName = "Ana Perez",
            email = "ana@salestrack.app",
            role = UserRole.Admin,
        ),
        TeamMember(
            id = "U-002",
            fullName = "Luis Gomez",
            email = "luis@salestrack.app",
            role = UserRole.Supervisor,
        ),
        TeamMember(
            id = "U-003",
            fullName = "Marta Diaz",
            email = "marta@salestrack.app",
            role = UserRole.Seller,
        ),
    )
}

