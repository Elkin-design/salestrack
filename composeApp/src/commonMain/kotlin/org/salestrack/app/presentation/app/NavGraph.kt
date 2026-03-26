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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import org.salestrack.app.presentation.feature.dashboard.DashboardRoute
import org.salestrack.app.presentation.feature.sales.SalesRoute

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
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    var currentDestination by remember { mutableStateOf(AppDestination.Dashboard) }
    val container = rememberAppContainer()

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
        modifier = modifier,
    ) { paddingValues ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)

        when (currentDestination) {
            AppDestination.Dashboard -> DashboardRoute(container = container, modifier = screenModifier)
            AppDestination.Sales -> SalesRoute(container = container, modifier = screenModifier)
            AppDestination.Inventory -> PlaceholderScreen("Inventario", screenModifier)
            AppDestination.Reports -> PlaceholderScreen("Reportes", screenModifier)
            AppDestination.Team -> PlaceholderScreen("Equipo", screenModifier)
            AppDestination.Settings -> PlaceholderScreen("Configuración", screenModifier)
        }
    }
}
