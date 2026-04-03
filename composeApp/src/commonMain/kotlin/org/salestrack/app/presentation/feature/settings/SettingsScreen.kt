package org.salestrack.app.presentation.feature.settings

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.salestrack.app.domain.model.AppThemeMode
import org.salestrack.app.domain.model.CurrencyCode
import org.salestrack.app.presentation.app.AppContainer
import org.salestrack.app.presentation.feature.backup.BackupRoute
import org.salestrack.app.presentation.feature.category.CategoryManagementRoute
import org.salestrack.app.presentation.feature.export.ExportReportRoute
import org.salestrack.app.presentation.feature.notification.NotificationSettingsRoute
import org.salestrack.app.presentation.feature.print.PrintRoute

private enum class SettingsSection {
    Main,
    Categories,
    Notifications,
    Export,
    Print,
    Backup,
}

@Composable
fun SettingsRoute(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    var activeSection by remember { mutableStateOf(SettingsSection.Main) }

    when (activeSection) {
        SettingsSection.Categories -> {
            CategoryManagementRoute(
                container = container,
                onBack = { activeSection = SettingsSection.Main },
                modifier = modifier,
            )
            return
        }
        SettingsSection.Notifications -> {
            NotificationSettingsRoute(
                container = container,
                onBack = { activeSection = SettingsSection.Main },
                modifier = modifier,
            )
            return
        }
        SettingsSection.Export -> {
            ExportReportRoute(
                container = container,
                onBack = { activeSection = SettingsSection.Main },
                modifier = modifier,
            )
            return
        }
        SettingsSection.Print -> {
            PrintRoute(
                container = container,
                onBack = { activeSection = SettingsSection.Main },
                modifier = modifier,
            )
            return
        }
        SettingsSection.Backup -> {
            BackupRoute(
                container = container,
                onBack = { activeSection = SettingsSection.Main },
                modifier = modifier,
            )
            return
        }
        SettingsSection.Main -> Unit
    }

    val viewModel = remember {
        SettingsViewModel(
            dispatcherProvider = container.dispatcherProvider,
            observeSettingsUseCase = container.observeSettingsUseCase,
            updateSettingsUseCase = container.updateSettingsUseCase,
        )
    }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect {
            // Punto de extension para snackbar/toast.
        }
    }

    SettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onOpenCategoryManagement = { activeSection = SettingsSection.Categories },
        onOpenNotificationSettings = { activeSection = SettingsSection.Notifications },
        onOpenExport = { activeSection = SettingsSection.Export },
        onOpenPrint = { activeSection = SettingsSection.Print },
        onOpenBackup = { activeSection = SettingsSection.Backup },
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onOpenCategoryManagement: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenPrint: () -> Unit,
    onOpenBackup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Configuracion", style = MaterialTheme.typography.headlineSmall)

        if (uiState.isLoading) {
            Text("Cargando configuracion...")
            return@Column
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Moneda", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyCode.entries.forEach { currency ->
                        FilterChip(
                            selected = uiState.currency == currency,
                            onClick = { onEvent(SettingsUiEvent.CurrencyChanged(currency)) },
                            label = { Text(currency.name) },
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Formato y zona horaria", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.numberFormatLocale,
                    onValueChange = { onEvent(SettingsUiEvent.NumberFormatLocaleChanged(it)) },
                    label = { Text("Locale numerico") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = uiState.timeZoneId,
                    onValueChange = { onEvent(SettingsUiEvent.TimeZoneChanged(it)) },
                    label = { Text("Zona horaria") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Tema visual", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = uiState.themeMode == mode,
                            onClick = { onEvent(SettingsUiEvent.ThemeModeChanged(mode)) },
                            label = { Text(mode.name) },
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Tamano de fuente Desktop", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.desktopFontScale.toString(),
                    onValueChange = {
                        onEvent(
                            SettingsUiEvent.DesktopFontScaleChanged(
                                it.toFloatOrNull() ?: uiState.desktopFontScale,
                            ),
                        )
                    },
                    label = { Text("Escala 0.8 a 2.0") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Button(
            onClick = { onEvent(SettingsUiEvent.SaveClicked) },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (uiState.isSaving) "Guardando..." else "Guardar configuracion")
        }

        Button(
            onClick = onOpenCategoryManagement,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Gestionar categorias")
        }

        Button(
            onClick = onOpenNotificationSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Configurar notificaciones")
        }

        Button(
            onClick = onOpenExport,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Exportar reportes")
        }

        Button(
            onClick = onOpenPrint,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Imprimir reportes")
        }

        Button(
            onClick = onOpenBackup,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Generar backup")
        }

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
