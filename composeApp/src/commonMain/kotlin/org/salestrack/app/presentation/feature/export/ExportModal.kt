package org.salestrack.app.presentation.feature.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.salestrack.app.domain.model.ExportDestination
import org.salestrack.app.domain.model.ExportFormat
import org.salestrack.app.presentation.app.AppContainer

@Composable
fun ExportModal(
    container: AppContainer,
    onDismiss: () -> Unit,
) {
    val viewModel = remember {
        ExportReportViewModel(
            dispatcherProvider = container.dispatcherProvider,
            exportPdfUseCase = container.exportPdfUseCase,
            exportExcelUseCase = container.exportExcelUseCase,
            exportCsvUseCase = container.exportCsvUseCase,
            fileSaver = org.salestrack.app.core.utils.platformFileSaver,
        )
    }
    val uiState by viewModel.state.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Exportar Datos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
                    }
                }

                Text(
                    "Selecciona el formato para generar tu reporte profesional.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Format Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Formato de Archivo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportFormat.entries.forEach { format ->
                            FormatCard(
                                format = format,
                                selected = uiState.selectedFormat == format,
                                onClick = { viewModel.onEvent(ExportReportUiEvent.FormatChanged(format)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }


                // Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Incluir vendedor", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = uiState.includeSellerColumn,
                        onCheckedChange = { viewModel.onEvent(ExportReportUiEvent.IncludeSellerColumnChanged(it)) }
                    )
                }

                if (uiState.errorMessage != null) {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                if (uiState.savedArtifact != null) {
                    OutlinedButton(
                        onClick = { viewModel.onEvent(ExportReportUiEvent.OpenSavedFile) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !uiState.isOpening && !uiState.isExporting,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        if (uiState.isOpening) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Abriendo...")
                        } else {
                            Icon(Icons.Rounded.Description, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Abrir Archivo")
                        }
                    }
                }

                Button(
                    onClick = { viewModel.onEvent(ExportReportUiEvent.ExportClicked) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !uiState.isExporting
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.FileDownload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generar y Exportar")
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatCard(
    format: ExportFormat,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when(format) {
        ExportFormat.Pdf -> Icons.Rounded.PictureAsPdf
        ExportFormat.Excel -> Icons.Rounded.TableChart
        ExportFormat.Csv -> Icons.Rounded.Description
    }
    
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val onColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = color,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = onColor)
            Text(format.name, style = MaterialTheme.typography.labelMedium, color = onColor, fontWeight = FontWeight.Bold)
        }
    }
}
