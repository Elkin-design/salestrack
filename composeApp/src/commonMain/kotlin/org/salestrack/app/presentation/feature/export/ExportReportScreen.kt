package org.salestrack.app.presentation.feature.export

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.salestrack.app.domain.model.ExportDestination
import org.salestrack.app.domain.model.ExportFormat
import org.salestrack.app.presentation.app.AppContainer

@Composable
fun ExportReportRoute(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember {
        ExportReportViewModel(
            dispatcherProvider = container.dispatcherProvider,
            exportPdfUseCase = container.exportPdfUseCase,
            exportExcelUseCase = container.exportExcelUseCase,
            exportCsvUseCase = container.exportCsvUseCase,
        )
    }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect {
            // Punto de extension para snackbar/toast.
        }
    }

    ExportReportScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun ExportReportScreen(
    uiState: ExportReportUiState,
    onEvent: (ExportReportUiEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Exportar reportes", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Volver") }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Formato", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormat.entries.forEach { format ->
                        FilterChip(
                            selected = uiState.selectedFormat == format,
                            onClick = { onEvent(ExportReportUiEvent.FormatChanged(format)) },
                            label = { Text(format.name) },
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Destino", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportDestination.entries.forEach { destination ->
                        FilterChip(
                            selected = uiState.selectedDestination == destination,
                            onClick = { onEvent(ExportReportUiEvent.DestinationChanged(destination)) },
                            label = { Text(destination.name) },
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onEvent(ExportReportUiEvent.ExportClicked) },
            enabled = !uiState.isExporting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (uiState.isExporting) "Exportando..." else "Exportar")
        }

        if (uiState.lastResult != null) {
            Text("Resultado: ${uiState.lastResult}")
        }

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
