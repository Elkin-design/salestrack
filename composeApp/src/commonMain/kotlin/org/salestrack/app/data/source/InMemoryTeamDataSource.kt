package org.salestrack.app.data.source

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.salestrack.app.domain.model.TeamMember

class InMemoryTeamDataSource(initialMembers: List<TeamMember>) {
    private val membersState = MutableStateFlow(initialMembers)

    fun observe(): StateFlow<List<TeamMember>> = membersState.asStateFlow()

    fun getCurrent(): List<TeamMember> = membersState.value

    fun replaceAll(items: List<TeamMember>) {
        membersState.value = items
    }
}

