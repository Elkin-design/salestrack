package org.salestrack.app.presentation.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.domain.model.UserRole
import org.salestrack.app.presentation.app.AppContainer

@Composable
fun TeamRoute(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember {
        TeamViewModel(
            dispatcherProvider = container.dispatcherProvider,
            saleRepository = container.saleRepository,
            teamRepository = container.teamRepository,
            getTeamSalesUseCase = container.getTeamSalesUseCase,
            inviteMemberUseCase = container.inviteMemberUseCase,
            getRolePermissionsUseCase = container.getRolePermissionsUseCase,
        )
    }
    val uiState by viewModel.state.collectAsState()

    TeamScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun TeamScreen(
    uiState: TeamUiState,
    onEvent: (TeamUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canManageTeam = uiState.currentRole == UserRole.Admin
    val permissions = when (uiState.currentRole) {
        UserRole.Admin -> "Admin"
        UserRole.Supervisor -> "Supervisor"
        UserRole.Seller -> "Vendedor"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Equipo", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Text("Rol actual: $permissions", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserRole.entries.forEach { role ->
                    FilterChip(
                        selected = uiState.currentRole == role,
                        onClick = { onEvent(TeamUiEvent.ChangeCurrentRole(role)) },
                        label = { Text(role.name) },
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { onEvent(TeamUiEvent.ChangeCategory(null)) },
                    label = { Text("Todas") },
                )
                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onEvent(TeamUiEvent.ChangeCategory(category)) },
                        label = { Text(category) },
                    )
                }
            }
        }
        if (canManageTeam) {
            item {
                TeamInviteCard(
                    onInvite = { fullName, email, role ->
                        onEvent(TeamUiEvent.InviteMember(fullName, email, role))
                    },
                )
            }
        } else {
            item {
                Text(
                    text = "Solo el rol Admin puede gestionar miembros del equipo.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            Text("Ranking de vendedores", style = MaterialTheme.typography.titleMedium)
        }
        items(uiState.ranking, key = { it.memberId }) { member ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(member.memberName, style = MaterialTheme.typography.titleSmall)
                    Text("Rol: ${member.role.name}")
                    Text("Total vendido: $${formatMoney(member.totalSold)}")
                    Text("Transacciones: ${member.transactionCount}")
                    Text("Ticket promedio: $${formatMoney(member.averageTicket)}")
                }
            }
        }

        if (uiState.errorMessage != null) {
            item {
                Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TeamInviteCard(
    onInvite: (String, String, UserRole) -> Unit,
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.Seller) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Invitar miembro", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserRole.entries.forEach { availableRole ->
                    FilterChip(
                        selected = role == availableRole,
                        onClick = { role = availableRole },
                        label = { Text(availableRole.name) },
                    )
                }
            }
            Button(
                onClick = {
                    onInvite(fullName, email, role)
                    fullName = ""
                    email = ""
                    role = UserRole.Seller
                },
            ) {
                Text("Invitar")
            }
        }
    }
}


