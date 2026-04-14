package org.salestrack.app.presentation.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onOpenExport: () -> Unit, // Aunque no se usen en el UI, los mantengo por compatibilidad con la firma si es necesario
    onOpenPrint: () -> Unit,
    onOpenBackup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Gestiona tus preferencias y datos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        bottomBar = {
            if (!uiState.isLoading) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onEvent(SettingsUiEvent.SaveClicked) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Guardando...")
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Sección: Localización y Moneda
                SettingsGroup(title = "Localización y Moneda") {
                    SettingsItem(
                        icon = Icons.Default.CurrencyExchange,
                        title = "Moneda del Sistema",
                        subtitle = "Selecciona la moneda principal para tus ventas"
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CurrencyCode.entries.forEach { currency ->
                                FilterChip(
                                    selected = uiState.currency == currency,
                                    onClick = { onEvent(SettingsUiEvent.CurrencyChanged(currency)) },
                                    label = { Text(currency.name) },
                                    leadingIcon = if (uiState.currency == currency) {
                                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    SettingsTextFieldItem(
                        icon = Icons.Default.Language,
                        title = "Formato Regional (Locale)",
                        value = uiState.numberFormatLocale,
                        onValueChange = { onEvent(SettingsUiEvent.NumberFormatLocaleChanged(it)) },
                        placeholder = "Ej: es-CO"
                    )

                    SettingsTextFieldItem(
                        icon = Icons.Default.Schedule,
                        title = "Zona Horaria",
                        value = uiState.timeZoneId,
                        onValueChange = { onEvent(SettingsUiEvent.TimeZoneChanged(it)) },
                        placeholder = "Ej: America/Bogota"
                    )
                }

                // Sección: Personalización Visual
                SettingsGroup(title = "Apariencia") {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Tema de la Aplicación",
                        subtitle = "Ajusta el modo visual según tu preferencia"
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = uiState.themeMode == mode,
                                    onClick = { onEvent(SettingsUiEvent.ThemeModeChanged(mode)) },
                                    label = { Text(mode.name) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    SettingsTextFieldItem(
                        icon = Icons.Default.TextFormat,
                        title = "Escala de Fuente (Desktop)",
                        value = uiState.desktopFontScale.toString(),
                        onValueChange = {
                            val newValue = it.toFloatOrNull() ?: uiState.desktopFontScale
                            onEvent(SettingsUiEvent.DesktopFontScaleChanged(newValue))
                        },
                        placeholder = "Escala 0.8 a 2.0"
                    )
                }

                // Sección: Gestión
                SettingsGroup(title = "Gestión") {
                    SettingsActionItem(
                        icon = Icons.Default.Category,
                        title = "Categorías",
                        subtitle = "Gestionar familias de productos",
                        onClick = onOpenCategoryManagement
                    )
                    SettingsActionItem(
                        icon = Icons.Default.Notifications,
                        title = "Notificaciones",
                        subtitle = "Alertas de inventario y cierres",
                        onClick = onOpenNotificationSettings
                    )
                }

                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(12.dp))
                            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    content: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        content?.invoke()
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SettingsTextFieldItem(
    icon: ImageVector,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    SettingsItem(icon = icon, title = title) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )
    }
}
