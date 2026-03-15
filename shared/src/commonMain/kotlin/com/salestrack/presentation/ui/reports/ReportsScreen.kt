package com.salestrack.presentation.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salestrack.presentation.viewmodel.ReportViewModel
import com.salestrack.presentation.viewmodel.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { reportViewModel.exportToPdf("Reporte_Ventas", sales) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exportar PDF")
                    }
                    Button(
                        onClick = { reportViewModel.exportToExcel("Reporte_Ventas", sales) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exportar Excel")
                    }
                }

                val exportStatus by reportViewModel.exportState.collectAsState()
                
                when (val status = exportStatus) {
                    is ReportViewModel.ExportStatus.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    is ReportViewModel.ExportStatus.Success -> {
                        Text(
                            "Exportado con éxito: ${status.fileName}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    is ReportViewModel.ExportStatus.Error -> {
                        Text(
                            "Error: ${status.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> {}
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
