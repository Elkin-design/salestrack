package org.salestrack.app.presentation.feature.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.presentation.app.AppContainer

@Composable
fun ReportsRoute(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember {
        ReportsViewModel(
            dispatcherProvider = container.dispatcherProvider,
            timeProvider = container.timeProvider,
            repository = container.saleRepository,
            getPeriodReportUseCase = container.getPeriodReportUseCase,
        )
    }
    val uiState by viewModel.state.collectAsState()

    ReportsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
fun ReportsScreen(
    uiState: ReportsUiState,
    onEvent: (ReportsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Reportes", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReportPeriod.entries.forEach { period ->
                    FilterChip(
                        selected = uiState.selectedPeriod == period,
                        onClick = { onEvent(ReportsUiEvent.ChangePeriod(period)) },
                        label = { Text(period.name) },
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { onEvent(ReportsUiEvent.ChangeCategory(null)) },
                    label = { Text("Todas") },
                )
                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onEvent(ReportsUiEvent.ChangeCategory(category)) },
                        label = { Text(category) },
                    )
                }
            }
        }

        uiState.report?.let { report ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ReportKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Total",
                        value = "$${formatMoney(report.summary.totalAmount)}",
                    )
                    ReportKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Transacciones",
                        value = report.summary.transactionCount.toString(),
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ReportKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Ticket promedio",
                        value = "$${formatMoney(report.summary.averageTicket)}",
                    )
                    ReportKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Top volumen",
                        value = report.summary.topProductByVolume,
                    )
                }
            }
            item {
                Text("Desglose por categoría", style = MaterialTheme.typography.titleMedium)
            }
            items(report.summary.categoryBreakdown, key = { it.category }) { category ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(category.category)
                        Text("$${formatMoney(category.amount)}", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                Text("Serie por periodo", style = MaterialTheme.typography.titleMedium)
            }
            items(report.points, key = { it.label }) { point ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(point.label, style = MaterialTheme.typography.titleSmall)
                        Text("Total: $${formatMoney(point.totalAmount)}")
                        Text("Ventas: ${point.transactionCount}")
                    }
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
private fun ReportKpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

