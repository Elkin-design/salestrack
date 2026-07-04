package org.salestrack.app.presentation.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.presentation.feature.auth.LoginScreen
import org.salestrack.app.presentation.feature.auth.AuthUiState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.collectLatest

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
        route = AppDestination.Settings,
        icon = Icons.Rounded.Settings,
        label = "Ajustes",
        contentDescription = "Configuración de la aplicación",
    ),
)

enum class AppDestination {
    Login,
    Dashboard,
    Sales,
    Pos,
    Inventory,
    Reports,
    Settings,
    Export,
    Print,
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()
    val authViewModel = container.authViewModel
    val authUiState by authViewModel.uiState.collectAsState()
    
    var currentDestination by remember {
        mutableStateOf(if (authUiState.isAuthenticated) AppDestination.Dashboard else AppDestination.Login)
    }
    var initialReportPeriod by remember { mutableStateOf(ReportPeriod.Daily) }

    // Manejo de la navegación obligatoria basada en la autenticación
    LaunchedEffect(authUiState.isAuthenticated) {
        if (!authUiState.isAuthenticated) {
            currentDestination = AppDestination.Login
        } else if (currentDestination == AppDestination.Login) {
            currentDestination = AppDestination.Dashboard
        }
    }

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

    Column(modifier = keyboardModifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            val screenModifier = Modifier.fillMaxSize()

            when (currentDestination) {
                AppDestination.Login -> LoginScreen(
                    viewModel = authViewModel,
                    modifier = screenModifier
                )
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
                    DashboardRoute(
                        viewModel = viewModel,
                        onNavigate = { destination ->
                            currentDestination = destination
                        },
                        onNavigateWithPeriod = { destination, period ->
                            initialReportPeriod = period
                            currentDestination = destination
                        },
                        container = container,
                        modifier = screenModifier
                    )
                }
                AppDestination.Sales -> SalesRoute(
                    container = container, 
                    onNavigateToPos = { currentDestination = AppDestination.Pos },
                    modifier = screenModifier
                )
                AppDestination.Pos -> org.salestrack.app.presentation.feature.sales.PosRoute(
                    container = container,
                    onNavigateBack = { currentDestination = AppDestination.Sales },
                    modifier = screenModifier
                )
                AppDestination.Inventory -> InventoryRoute(container = container, modifier = screenModifier)
                AppDestination.Reports -> ReportsRoute(
                    container = container, 
                    initialPeriod = initialReportPeriod,
                    onBack = { currentDestination = AppDestination.Dashboard },
                    modifier = screenModifier
                )
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

        if (currentDestination != AppDestination.Login) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(56.dp)
            ) {
                destinations.forEach { destination ->
                    val selected = destination.route == currentDestination
                    
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.1f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )

                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = destination.route },
                        modifier = Modifier.semantics {
                            contentDescription = destination.contentDescription
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.contentDescription,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(scale)
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp
                                )
                            )
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}
