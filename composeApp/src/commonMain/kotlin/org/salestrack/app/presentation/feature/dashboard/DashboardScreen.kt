package org.salestrack.app.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.presentation.feature.dashboard.components.CategoryBreakdownCard
import org.salestrack.app.presentation.feature.dashboard.components.DashboardHeader
import org.salestrack.app.presentation.feature.dashboard.components.EmptyStateCard
import org.salestrack.app.presentation.feature.dashboard.components.KpiSection
import org.salestrack.app.presentation.feature.dashboard.components.RecentSaleCardItem
import org.salestrack.app.presentation.feature.dashboard.components.RecentSalesSection
import org.salestrack.app.presentation.feature.dashboard.components.ReportsQuickSection
import org.salestrack.app.presentation.feature.dashboard.components.StockAlertCardItem
import org.salestrack.app.presentation.feature.dashboard.components.StockAlertsSection
import org.salestrack.app.presentation.feature.dashboard.components.WeeklyTrendCard
import org.salestrack.app.presentation.app.AppDestination
import org.salestrack.app.presentation.app.AppContainer
import org.salestrack.app.presentation.feature.export.ExportModal
import org.salestrack.app.domain.model.ReportPeriod

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    onNavigate: (AppDestination) -> Unit,
    onNavigateWithPeriod: (AppDestination, ReportPeriod) -> Unit,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DashboardUiEffect.ShowMessage -> {
                    // Show message and manually close after 1.8 seconds for a "fast" feel
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = if (effect.isSuccess) "SUCCESS" else "ERROR",
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                    delay(1800)
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
                is DashboardUiEffect.NavigateToDestination -> {
                    if (effect is DashboardUiEffect.NavigateToDestination.NavigateToReportsWithPeriod) {
                        onNavigateWithPeriod(effect.destination, effect.period)
                    } else {
                        onNavigate(effect.destination)
                    }
                }
            }
        }
    }

    DashboardScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = { viewModel.onEvent(DashboardUiEvent.Refresh) },
        onNavigateToReports = { viewModel.onEvent(DashboardUiEvent.NavigateToReports(it)) },
        onNavigateToExport = { viewModel.onEvent(DashboardUiEvent.NavigateToExport) },
        onDismissExportModal = { viewModel.onEvent(DashboardUiEvent.ToggleExportModal(false)) },
        container = container,
        modifier = modifier,
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onNavigateToReports: (ReportPeriod) -> Unit,
    onNavigateToExport: () -> Unit,
    onDismissExportModal: () -> Unit,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (uiState.showExportModal) {
            ExportModal(
                container = container,
                onDismiss = onDismissExportModal
            )
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DashboardHeader(
                transactionCountToday = uiState.summary.transactionCountToday,
                isLoading = uiState.isLoading,
                onRefresh = onRefresh,
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    ErrorPanel(
                        message = uiState.errorMessage,
                        onRefresh = onRefresh,
                    )
                }

                else -> {
                    DashboardContent(
                        uiState = uiState,
                        onNavigateToReports = onNavigateToReports,
                        onNavigateToExport = onNavigateToExport,
                    )
                }
            }
        }

        // Floating Message Overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
                .fillMaxWidth(0.92f),
            contentAlignment = Alignment.TopCenter
        ) {
            SnackbarHost(
                hostState = snackbarHostState,
            ) { data ->
                val isSuccess = data.visuals.actionLabel == "SUCCESS"
                val containerColor = if (isSuccess) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.97f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.97f)
                }
                val contentColor = if (isSuccess) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = contentColor.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToReports: (ReportPeriod) -> Unit,
    onNavigateToExport: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 980.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                KpiSection(
                    summary = uiState.summary,
                    lowStockCount = uiState.lowStockProducts.size,
                    isWide = isWide,
                )
            }

            item {
                ReportsQuickSection(
                    onNavigateToReports = { periodStr ->
                        val period = when(periodStr) {
                            "MONTH" -> ReportPeriod.Monthly
                            "WEEK" -> ReportPeriod.Weekly
                            else -> ReportPeriod.Daily
                        }
                        onNavigateToReports(period)
                    },
                    onNavigateToExport = onNavigateToExport,
                )
            }

            if (isWide) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        WeeklyTrendCard(
                            trend = uiState.weeklyTrend,
                            modifier = Modifier.weight(1.5f),
                        )
                        CategoryBreakdownCard(
                            breakdown = uiState.categoryBreakdown,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                item { WeeklyTrendCard(trend = uiState.weeklyTrend) }
                item { CategoryBreakdownCard(breakdown = uiState.categoryBreakdown) }
            }

            item {
                RecentSalesSection(salesCount = uiState.recentSales.size) {
                    if (uiState.recentSales.isEmpty()) {
                        EmptyStateCard("No hay ventas recientes. Registra ventas para activar el panel operativo.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.recentSales.forEach { sale ->
                                RecentSaleCardItem(
                                    saleTitle = sale.productName,
                                    saleMeta = "${sale.quantity} x $${formatMoney(sale.unitPrice)} · ${sale.category}",
                                    total = "$${formatMoney(sale.netTotal)}",
                                )
                            }
                        }
                    }
                }
            }

            item {
                StockAlertsSection(alertsCount = uiState.lowStockProducts.size) {
                    if (uiState.lowStockProducts.isEmpty()) {
                        EmptyStateCard("Todo en orden. No hay productos con stock crítico.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.lowStockProducts.forEach { product ->
                                StockAlertCardItem(
                                    productName = product.name,
                                    stockInfo = "Stock actual: ${product.stock} ${product.unit} (Umbral: ${product.minimumStock})",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorPanel(
    message: String,
    onRefresh: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "No se pudo cargar el dashboard",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRefresh) {
                Text("Reintentar")
            }
        }
    }
}
