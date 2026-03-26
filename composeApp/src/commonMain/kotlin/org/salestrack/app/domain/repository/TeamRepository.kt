package org.salestrack.app.domain.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.UserRole

interface TeamRepository {
    fun observeMembers(): Flow<List<TeamMember>>
    suspend fun inviteMember(fullName: String, email: String, role: UserRole): AppResult<TeamMember>
    suspend fun updateMemberRole(memberId: String, role: UserRole): AppResult<TeamMember>
    suspend fun removeMember(memberId: String): AppResult<Unit>
}

