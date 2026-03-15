package com.salestrack.presentation.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onNavigateToRegisterSale: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("SalesTrack Dashboard") },
                actions = {
                    Button(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToRegisterSale) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Bienvenido a SalesTrack", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("Aquí verás el resumen de tus ventas e inventario.")
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = onNavigateToCatalog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Catálogo de Productos")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onNavigateToReports,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver Reportes y Estadísticas")
            }
        }
    }
}
