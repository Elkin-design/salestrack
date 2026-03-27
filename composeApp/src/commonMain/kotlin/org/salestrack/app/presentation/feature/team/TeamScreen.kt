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
import androidx.compose.material3.CardDefaults
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
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.model.TeamMember
import org.salestrack.app.domain.model.TeamMemberPerformance
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
    val canManageTeam = uiState.permissions.canManageTeam
    val roleLabel = uiState.currentRole.asLabel()
    val selectedPerformance = uiState.ranking.firstOrNull { it.memberId == uiState.selectedMemberId }
    val selectedMember = uiState.members.firstOrNull { it.id == uiState.selectedMemberId }
    val teamTotal = uiState.ranking.sumOf { it.totalSold }
    val teamTransactions = uiState.ranking.sumOf { it.transactionCount }

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
            Text("Rol actual: $roleLabel", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserRole.entries.forEach { role ->
                    FilterChip(
                        selected = uiState.currentRole == role,
                        onClick = { onEvent(TeamUiEvent.ChangeCurrentRole(role)) },
                        label = { Text(role.asLabel()) },
                    )
                }
            }
        }
        item {
            TeamPermissionsCard(uiState = uiState)
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
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Reporte consolidado del equipo", style = MaterialTheme.typography.titleMedium)
                    Text("Total vendido: $${formatMoney(teamTotal)}")
                    Text("Transacciones: $teamTransactions")
                    Text("Vendedores activos: ${uiState.ranking.size}")
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
        if (uiState.ranking.isEmpty()) {
            item {
                Text("No hay vendedores para mostrar con los filtros actuales.")
            }
        }
        items(uiState.ranking, key = { it.memberId }) { member ->
            val isSelected = member.memberId == uiState.selectedMemberId
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                ),
                onClick = { onEvent(TeamUiEvent.SelectMember(member.memberId)) },
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(member.memberName, style = MaterialTheme.typography.titleSmall)
                    Text("Rol: ${member.role.asLabel()}")
                    Text("Total vendido: $${formatMoney(member.totalSold)}")
                    Text("Transacciones: ${member.transactionCount}")
                    Text("Ticket promedio: $${formatMoney(member.averageTicket)}")
                }
            }
        }
        item {
            TeamMemberDetailCard(
                selectedMember = selectedMember,
                selectedPerformance = selectedPerformance,
                selectedMemberSales = uiState.selectedMemberSales,
                canManageTeam = canManageTeam,
                onChangeRole = { role ->
                    selectedMember?.let { onEvent(TeamUiEvent.ChangeMemberRole(it.id, role)) }
                },
                onRemove = {
                    selectedMember?.let { onEvent(TeamUiEvent.RemoveMember(it.id)) }
                },
            )
        }

        if (uiState.errorMessage != null) {
            item {
                Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TeamPermissionsCard(uiState: TeamUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Permisos del rol", style = MaterialTheme.typography.titleSmall)
            Text(if (uiState.permissions.canViewAllSales) "- Puede ver todas las ventas" else "- No puede ver todas las ventas")
            Text(if (uiState.permissions.canManageTeam) "- Puede gestionar el equipo" else "- No puede gestionar el equipo")
            Text(if (uiState.permissions.canViewOnlyOwnSales) "- Solo puede ver ventas propias" else "- Puede ver ventas de otros")
        }
    }
}

@Composable
private fun TeamMemberDetailCard(
    selectedMember: TeamMember?,
    selectedPerformance: TeamMemberPerformance?,
    selectedMemberSales: List<Sale>,
    canManageTeam: Boolean,
    onChangeRole: (UserRole) -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Detalle del vendedor", style = MaterialTheme.typography.titleMedium)

            if (selectedMember == null || selectedPerformance == null) {
                Text("Selecciona un vendedor para ver su detalle.")
                return@Column
            }

            Text(selectedMember.fullName, style = MaterialTheme.typography.titleSmall)
            Text(selectedMember.email)
            Text("Rol actual: ${selectedMember.role.asLabel()}")
            Text("Total vendido: $${formatMoney(selectedPerformance.totalSold)}")

            if (canManageTeam) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserRole.entries.forEach { role ->
                        FilterChip(
                            selected = selectedMember.role == role,
                            onClick = { onChangeRole(role) },
                            label = { Text(role.asLabel()) },
                        )
                    }
                }
                Button(onClick = onRemove) {
                    Text("Eliminar miembro")
                }
            }

            Text("Ventas recientes", style = MaterialTheme.typography.titleSmall)
            if (selectedMemberSales.isEmpty()) {
                Text("Este vendedor no tiene ventas con el filtro actual.")
            } else {
                selectedMemberSales.take(5).forEach { sale ->
                    Text("${formatEpochMillis(sale.createdAtMillis)} - ${sale.productName} - $${formatMoney(sale.netTotal)}")
                }
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
                enabled = fullName.isNotBlank() && email.isNotBlank(),
            ) {
                Text("Invitar")
            }
        }
    }
}

private fun UserRole.asLabel(): String = when (this) {
    UserRole.Admin -> "Administrador"
    UserRole.Supervisor -> "Supervisor"
    UserRole.Seller -> "Vendedor"
}

private fun formatEpochMillis(epochMillis: Long): String {
    return "UTC ms: $epochMillis"
}


