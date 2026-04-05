package org.salestrack.app.presentation.feature.dashboard

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.presentation.app.AppContainer
import org.salestrack.app.presentation.component.KpiCard

@Composable
fun DashboardRoute(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember {
        DashboardViewModel(
            dispatcherProvider = container.dispatcherProvider,
            repository = container.saleRepository,
            timeProvider = container.timeProvider,
            buildSummary = container.buildDashboardSummaryUseCase,
            filterSalesUseCase = container.filterSalesUseCase,
        )
    }
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DashboardUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    DashboardScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = { viewModel.onEvent(DashboardUiEvent.Refresh) },
        modifier = modifier,
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SnackbarHost(hostState = snackbarHostState)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineSmall,
            )
            Button(onClick = onRefresh) {
                Text("Actualizar")
            }
        }

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.errorMessage != null -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Error al cargar dashboard",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                title = "Ventas hoy",
                                value = "$${formatMoney(uiState.summary.totalSoldToday)}",
                            )
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                title = "Transacciones",
                                value = uiState.summary.transactionCountToday.toString(),
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                title = "Top producto",
                                value = uiState.summary.topProductToday,
                            )
                            KpiCard(
                                modifier = Modifier.weight(1f),
                                title = "Sincronización",
                                value = uiState.summary.syncStatus,
                            )
                        }
                    }
                    item {
                        Text(
                            text = "Últimas ventas",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    if (uiState.recentSales.isEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "No hay ventas recientes",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    } else {
                        items(uiState.recentSales, key = { it.id }) { sale ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = sale.productName, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${sale.quantity} x $${formatMoney(sale.unitPrice)} · ${sale.category}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = "Total neto: $${formatMoney(sale.netTotal)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


