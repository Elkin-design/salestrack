package org.salestrack.app.presentation.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.presentation.app.AppContainer
import kotlin.math.roundToInt

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
            getLowStockProducts = container.getLowStockProductsUseCase,
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFECE8F8), MaterialTheme.colorScheme.background),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SnackbarHost(hostState = snackbarHostState)

            DashboardHeaderCard(
                transactionCountToday = uiState.summary.transactionCountToday,
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
                    DashboardContent(uiState = uiState)
                }
            }
        }
    }
}

@Composable
private fun DashboardHeaderCard(
    transactionCountToday: Int,
    onRefresh: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F2FF)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Panel ejecutivo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${transactionCountToday} transacciones hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            Button(onClick = onRefresh) {
                Text("Actualizar")
            }
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 980.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                KpiSection(
                    summary = uiState.summary,
                    lowStockCount = uiState.lowStockProducts.size,
                    isWide = isWide,
                )
            }

            if (isWide) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
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

            // Removed OperationalInfoCard section to prioritize inventory KPIs

            item {
                Text(
                    text = "Ultimas ventas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (uiState.recentSales.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = "No hay ventas recientes. Registra ventas para activar el panel operativo.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(uiState.recentSales, key = { it.id }) { sale ->
                    RecentSaleCard(
                        saleTitle = sale.productName,
                        saleMeta = "${sale.quantity} x $${formatMoney(sale.unitPrice)} · ${sale.category}",
                        total = "$${formatMoney(sale.netTotal)}",
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Alertas de Stock",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (uiState.lowStockProducts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = "No hay productos con stock critico. Todo en orden.",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(uiState.lowStockProducts, key = { it.id }) { product ->
                    RecentSaleCard(
                        saleTitle = product.name,
                        saleMeta = "Stock: ${product.stock} ${product.unit} (Min: ${product.minimumStock})",
                        total = "!!", // Or we can create an Alert card, but reusing RecentSaleCard for now. Actually let's use a specialized layout inline or adapt RecentSaleCard parameters.
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiSection(
    summary: org.salestrack.app.domain.model.DashboardSummary,
    lowStockCount: Int,
    isWide: Boolean,
) {
    if (isWide) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KpiMetricCard(
                title = "Ventas hoy",
                value = "$${formatMoney(summary.totalSoldToday)}",
                subtitle = "Flujo neto diario",
                gradient = listOf(Color(0xFFFF9A7A), Color(0xFFF35EA3)),
                modifier = Modifier.weight(1f),
            )
            KpiMetricCard(
                title = "Ordenes",
                value = summary.transactionCountToday.toString(),
                subtitle = "Transacciones efectivas",
                gradient = listOf(Color(0xFF6BB7FF), Color(0xFF2A7DE1)),
                modifier = Modifier.weight(1f),
            )
            KpiMetricCard(
                title = "Producto top",
                value = summary.topProductToday,
                subtitle = "Mayor salida del dia",
                gradient = listOf(Color(0xFF56D8C6), Color(0xFF15B5A3)),
                modifier = Modifier.weight(1f),
            )
            KpiMetricCard(
                title = "Stock critico",
                value = lowStockCount.toString(),
                subtitle = "Por agotarse",
                gradient = listOf(Color(0xFFFF7A7A), Color(0xFFF03E3E)),
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KpiMetricCard(
                    title = "Ventas hoy",
                    value = "$${formatMoney(summary.totalSoldToday)}",
                    subtitle = "Flujo neto diario",
                    gradient = listOf(Color(0xFFFF9A7A), Color(0xFFF35EA3)),
                    modifier = Modifier.weight(1f),
                )
                KpiMetricCard(
                    title = "Ordenes",
                    value = summary.transactionCountToday.toString(),
                    subtitle = "Transacciones efectivas",
                    gradient = listOf(Color(0xFF6BB7FF), Color(0xFF2A7DE1)),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KpiMetricCard(
                    title = "Producto top",
                    value = summary.topProductToday,
                    subtitle = "Mayor salida del dia",
                    gradient = listOf(Color(0xFF56D8C6), Color(0xFF15B5A3)),
                    modifier = Modifier.weight(1f),
                )
                KpiMetricCard(
                    title = "Stock critico",
                    value = lowStockCount.toString(),
                    subtitle = "Por agotarse",
                    gradient = listOf(Color(0xFFFF7A7A), Color(0xFFF03E3E)),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.heightIn(min = 120.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = gradient))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.95f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
private fun WeeklyTrendCard(
    trend: List<DashboardTrendPoint>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Tendencia 7 dias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            val maxAmount = trend.maxOfOrNull { it.amount }?.takeIf { it > 0.0 } ?: 1.0
            if (trend.isEmpty()) {
                Text("Sin datos suficientes para tendencia", style = MaterialTheme.typography.bodyMedium)
            } else {
                trend.forEach { point ->
                    val ratio = (point.amount / maxAmount).toFloat().coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.width(34.dp),
                        )
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                        )
                        Text(
                            text = "$${formatMoney(point.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(78.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    breakdown: List<DashboardCategoryShare>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Mix por categoria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            val total = breakdown.sumOf { it.amount }
            if (breakdown.isEmpty() || total <= 0.0) {
                Text("Sin ventas categorizadas todavia", style = MaterialTheme.typography.bodyMedium)
            } else {
                breakdown.forEach { item ->
                    val ratio = (item.amount / total).toFloat().coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(item.category, style = MaterialTheme.typography.labelLarge)
                            Text(formatRatio(ratio), style = MaterialTheme.typography.labelLarge)
                        }
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationalInfoCard(
    title: String,
    value: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun RecentSaleCard(
    saleTitle: String,
    saleMeta: String,
    total: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = saleTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(saleMeta, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = total,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
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
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "No se pudo cargar el dashboard",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onRefresh) {
                Text("Reintentar")
            }
        }
    }
}

private fun formatRatio(value: Float): String {
    return "${(value * 100).roundToInt()}%"
}


