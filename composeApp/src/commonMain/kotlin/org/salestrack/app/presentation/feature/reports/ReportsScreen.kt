package org.salestrack.app.presentation.feature.reports

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.presentation.app.AppContainer
import org.salestrack.app.presentation.feature.reports.components.AnalyticsBarChart
import org.salestrack.app.presentation.feature.reports.components.AnalyticsLineChart
import androidx.compose.foundation.shape.CircleShape

@Composable
fun ReportsRoute(
    container: AppContainer,
    initialPeriod: ReportPeriod,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember {
        ReportsViewModel(
            dispatcherProvider = container.dispatcherProvider,
            timeProvider = container.timeProvider,
            repository = container.saleRepository,
            getPeriodReportUseCase = container.getPeriodReportUseCase,
            initialPeriod = initialPeriod,
        )
    }
    val uiState by viewModel.state.collectAsState()

    ReportsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun ReportsScreen(
    uiState: ReportsUiState,
    onEvent: (ReportsUiEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Period Selector
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                    Text(
                        "Análisis de Negocio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onEvent(ReportsUiEvent.Refresh) }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Sincronizar")
                    }
                }

                SecondaryScrollableTabRow(
                    selectedTabIndex = ReportPeriod.entries.toList().indexOf(uiState.selectedPeriod),
                    containerColor = Color.Transparent,
                    divider = {},
                    edgePadding = 0.dp,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(ReportPeriod.entries.toList().indexOf(uiState.selectedPeriod)),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    ReportPeriod.entries.forEach { period ->
                        val selected = uiState.selectedPeriod == period
                        Tab(
                            selected = selected,
                            onClick = { onEvent(ReportsUiEvent.ChangePeriod(period)) },
                            text = {
                                Text(
                                    text = when(period) {
                                        ReportPeriod.Daily -> "Hoy"
                                        ReportPeriod.Weekly -> "Semana"
                                        ReportPeriod.Monthly -> "Mes"
                                        ReportPeriod.Annual -> "Año"
                                        ReportPeriod.Custom -> "Personal"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState.isLoading) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.report != null) {
                val report = uiState.report
                
                // Chart Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Tendencia de Ventas",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Ingresos por periodo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.AutoMirrored.Rounded.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            if (uiState.selectedPeriod == ReportPeriod.Daily) {
                                AnalyticsBarChart(points = report.points)
                            } else {
                                AnalyticsLineChart(points = report.points)
                            }
                        }
                    }
                }

                // KPIs Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiCard(
                                title = "Ventas Totales",
                                value = "$${formatMoney(report.summary.totalAmount)}",
                                icon = Icons.Rounded.AccountBalanceWallet,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Transacciones",
                                value = report.summary.transactionCount.toString(),
                                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            KpiCard(
                                title = "Ticket Promedio",
                                value = "$${formatMoney(report.summary.averageTicket)}",
                                icon = Icons.Rounded.ConfirmationNumber,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                title = "Top Producto",
                                value = report.summary.topProductByVolume.ifBlank { "N/A" },
                                icon = Icons.Rounded.Star,
                                color = Color(0xFFFFA000),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Categories breakdown
                item {
                    Text(
                        "Distribución por Categoría",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(report.summary.categoryBreakdown) { category ->
                    CategoryItem(
                        name = category.category,
                        amount = category.amount,
                        percentage = (category.amount / report.summary.totalAmount).toFloat()
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun CategoryItem(
    name: String,
    amount: Double,
    percentage: Float
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("$${formatMoney(amount)}", fontWeight = FontWeight.Bold)
                Text("${(percentage * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

