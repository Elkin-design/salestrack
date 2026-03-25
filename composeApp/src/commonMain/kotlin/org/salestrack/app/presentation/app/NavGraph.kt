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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

data class NavigationDestination(
    val route: AppDestination,
    val label: String,
    val emoji: String,
    val contentDescription: String,
)

private val destinations = listOf(
    NavigationDestination(
        route = AppDestination.Dashboard,
        label = "Dashboard",
        emoji = "📊",
        contentDescription = "Panel de control",
    ),
    NavigationDestination(
        route = AppDestination.Sales,
        label = "Ventas",
        emoji = "🛒",
        contentDescription = "Gestión de ventas",
    ),
    NavigationDestination(
        route = AppDestination.Inventory,
        label = "Inventario",
        emoji = "📦",
        contentDescription = "Gestión de inventario",
    ),
    NavigationDestination(
        route = AppDestination.Reports,
        label = "Reportes",
        emoji = "📈",
        contentDescription = "Reportes y análisis",
    ),
    NavigationDestination(
        route = AppDestination.Team,
        label = "Equipo",
        emoji = "👥",
        contentDescription = "Gestión del equipo",
    ),
    NavigationDestination(
        route = AppDestination.Settings,
        label = "Configuración",
        emoji = "⚙️",
        contentDescription = "Configuración de la aplicación",
    ),
)

enum class AppDestination(val label: String) {
    Dashboard("Dashboard"),
    Sales("Ventas"),
    Inventory("Inventario"),
    Reports("Reportes"),
    Team("Equipo"),
    Settings("Configuración"),
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    var currentDestination by remember { mutableStateOf(AppDestination.Dashboard) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = destination.route == currentDestination,
                        onClick = { currentDestination = destination.route },
                        icon = {
                            Text(
                                text = destination.emoji,
                                fontSize = 20.sp,
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontSize = 10.sp,
                            )
                        },
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
            AppDestination.Dashboard -> PlaceholderScreen("Dashboard", screenModifier)
            AppDestination.Sales -> PlaceholderScreen("Ventas", screenModifier)
            AppDestination.Inventory -> PlaceholderScreen("Inventario", screenModifier)
            AppDestination.Reports -> PlaceholderScreen("Reportes", screenModifier)
            AppDestination.Team -> PlaceholderScreen("Equipo", screenModifier)
            AppDestination.Settings -> PlaceholderScreen("Configuración", screenModifier)
        }
    }
}

