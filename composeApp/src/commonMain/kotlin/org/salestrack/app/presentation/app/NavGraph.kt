package org.salestrack.app.presentation.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
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
    val emoji: String,
    val contentDescription: String,
)

private val destinations = listOf(
    NavigationDestination(
        route = AppDestination.Dashboard,
        emoji = "📊",
        contentDescription = "Panel de control",
    ),
    NavigationDestination(
        route = AppDestination.Sales,
        emoji = "🛒",
        contentDescription = "Gestión de ventas",
    ),
    NavigationDestination(
        route = AppDestination.Inventory,
        emoji = "📦",
        contentDescription = "Gestión de inventario",
    ),
    NavigationDestination(
        route = AppDestination.Reports,
        emoji = "📈",
        contentDescription = "Reportes y análisis",
    ),
    NavigationDestination(
        route = AppDestination.Team,
        emoji = "👥",
        contentDescription = "Gestión del equipo",
    ),
    NavigationDestination(
        route = AppDestination.Settings,
        emoji = "⚙️",
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
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = destination.route == currentDestination,
                        onClick = { currentDestination = destination.route },
                        modifier = Modifier.semantics {
                            contentDescription = destination.contentDescription
                        },
                        icon = {
                            Text(
                                text = destination.emoji,
                                fontSize = 20.sp,
                            )
                        },
                        label = null, // Sin label visible: en mobile solo se muestra el icono.
                        alwaysShowLabel = false,
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
            AppDestination.Dashboard -> DashboardRoute(container = container, modifier = screenModifier)
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
