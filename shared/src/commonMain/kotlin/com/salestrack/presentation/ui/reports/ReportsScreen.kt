package com.salestrack.presentation.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salestrack.presentation.viewmodel.ReportViewModel
import com.salestrack.presentation.viewmodel.SalesViewModel

@Composable
fun ReportsScreen(
    reportViewModel: ReportViewModel,
    salesViewModel: SalesViewModel,
    onBack: () -> Unit
) {
    val sales by salesViewModel.salesState.collectAsState()
    val dailyReport by reportViewModel.dailyReportState.collectAsState()

    LaunchedEffect(sales) {
        reportViewModel.generateDailyReport(sales)
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Reportes y Estadísticas") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Resumen Diario", style = MaterialTheme.typography.titleLarge)
            
            dailyReport?.let { report ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Vendido: $${report.totalSales}", style = MaterialTheme.typography.headlineMedium)
                        Text("Número de Ventas: ${report.count}")
                        Text("Producto Estrella: ${report.bestSellingProduct ?: "N/A"}")
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                Text("Desglose por Categoría", style = MaterialTheme.typography.titleMedium)
                report.categoryBreakdown.forEach { (cat, amount) ->
                    Text("$cat: $$amount", style = MaterialTheme.typography.bodyLarge)
                }
            } ?: Text("Cargando reporte o no hay ventas hoy...")
        }
    }
}
