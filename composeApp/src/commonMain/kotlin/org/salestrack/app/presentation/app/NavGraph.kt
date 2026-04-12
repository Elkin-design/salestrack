package org.salestrack.app.presentation.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import org.salestrack.app.presentation.feature.export.ExportReportRoute
import org.salestrack.app.presentation.feature.print.PrintRoute
import org.salestrack.app.presentation.feature.dashboard.DashboardRoute
import org.salestrack.app.presentation.feature.inventory.InventoryRoute
import org.salestrack.app.presentation.feature.reports.ReportsRoute
import org.salestrack.app.presentation.feature.sales.SalesRoute
import org.salestrack.app.presentation.feature.settings.SettingsRoute
import org.salestrack.app.presentation.feature.team.TeamRoute

data class NavigationDestination(
    val route: AppDestination,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String,
)

private val destinations = listOf(
    NavigationDestination(
        route = AppDestination.Dashboard,
        icon = Icons.Rounded.Dashboard,
        label = "Inicio",
        contentDescription = "Panel de control",
    ),
    NavigationDestination(
        route = AppDestination.Sales,
        icon = Icons.Rounded.ShoppingCart,
        label = "Ventas",
        contentDescription = "Gestión de ventas",
    ),
    NavigationDestination(
        route = AppDestination.Inventory,
        icon = Icons.Rounded.Inventory,
        label = "Inventario",
        contentDescription = "Gestión de inventario",
    ),
    NavigationDestination(
        route = AppDestination.Reports,
        icon = Icons.Rounded.BarChart,
        label = "Reportes",
        contentDescription = "Reportes y análisis",
    ),
    NavigationDestination(
        route = AppDestination.Team,
        icon = Icons.Rounded.Group,
        label = "Equipo",
        contentDescription = "Gestión del equipo",
    ),
    NavigationDestination(
        route = AppDestination.Settings,
        icon = Icons.Rounded.Settings,
        label = "Ajustes",
        contentDescription = "Configuración de la aplicación",
    ),
)

enum class AppDestination {
    Dashboard,
    Sales,
    Inventory,
    Reports,
    Team,
    Settings,
    Export,
    Print,
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    var currentDestination by remember { mutableStateOf(AppDestination.Dashboard) }
    val container = rememberAppContainer()
    val keyboardModifier = modifier.onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown || !event.isCtrlPressed) {
            return@onPreviewKeyEvent false
        }
        when (event.key) {
            Key.R -> {
                currentDestination = AppDestination.Reports
                true
            }
            Key.E -> {
                currentDestination = AppDestination.Export
                true
            }
            Key.P -> {
                currentDestination = AppDestination.Print
                true
            }
            else -> false
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.height(64.dp)
            ) {
                destinations.forEach { destination ->
                    val selected = destination.route == currentDestination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = destination.route },
                        modifier = Modifier.semantics {
                            contentDescription = destination.contentDescription
                        },
                        icon = {
                            val animatedScale by animateFloatAsState(
                                targetValue = if (selected) 1.25f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                            val iconColor by animateColorAsState(
                                targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.contentDescription,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(animatedScale)
                                    .then(
                                        if (selected) {
                                            Modifier.shadow(
                                                elevation = 12.dp,
                                                shape = CircleShape,
                                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            )
                                        } else Modifier
                                    )
                            )
                        },
                        label = null,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        modifier = keyboardModifier,
    ) { paddingValues ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)

        when (currentDestination) {
            AppDestination.Dashboard -> {
                val viewModel = remember(container) {
                    org.salestrack.app.presentation.feature.dashboard.DashboardViewModel(
                        dispatcherProvider = container.dispatcherProvider,
                        repository = container.saleRepository,
                        timeProvider = container.timeProvider,
                        buildSummary = container.buildDashboardSummaryUseCase,
                        filterSalesUseCase = container.filterSalesUseCase,
                        getLowStockProducts = container.getLowStockProductsUseCase,
                    )
                }
                DashboardRoute(viewModel = viewModel, modifier = screenModifier)
            }
            AppDestination.Sales -> SalesRoute(container = container, modifier = screenModifier)
            AppDestination.Inventory -> InventoryRoute(container = container, modifier = screenModifier)
            AppDestination.Reports -> ReportsRoute(container = container, modifier = screenModifier)
            AppDestination.Team -> TeamRoute(container = container, modifier = screenModifier)
            AppDestination.Settings -> SettingsRoute(container = container, modifier = screenModifier)
            AppDestination.Export -> ExportReportRoute(
                container = container,
                onBack = { currentDestination = AppDestination.Reports },
                modifier = screenModifier,
            )
            AppDestination.Print -> PrintRoute(
                container = container,
                onBack = { currentDestination = AppDestination.Reports },
                modifier = screenModifier,
            )
        }
    }
}
