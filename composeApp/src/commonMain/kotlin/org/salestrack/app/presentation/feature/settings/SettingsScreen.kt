package org.salestrack.app.presentation.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
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
            signOutUseCase = container.signOutUseCase,
            saleRepository = container.saleRepository,
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
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
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

                // Sección: Cuenta
                SettingsGroup(title = "Cuenta") {
                    SettingsActionItem(
                        icon = Icons.Default.Logout,
                        title = "Cerrar Sesión",
                        subtitle = "Salir de la cuenta actual",
                        onClick = { onEvent(SettingsUiEvent.SignOutClicked) }
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
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
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
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
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
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(20.dp)
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
                .padding(top = 10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )
    }
}
