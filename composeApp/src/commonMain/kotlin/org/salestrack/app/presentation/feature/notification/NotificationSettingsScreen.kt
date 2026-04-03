package org.salestrack.app.presentation.feature.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.salestrack.app.presentation.app.AppContainer

@Composable
fun NotificationSettingsRoute(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember {
        NotificationSettingsViewModel(
            dispatcherProvider = container.dispatcherProvider,
            observeNotificationSettingsUseCase = container.observeNotificationSettingsUseCase,
            updateNotificationSettingsUseCase = container.updateNotificationSettingsUseCase,
        )
    }

    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect {
            // Punto de extension para snackbar/toast.
        }
    }

    NotificationSettingsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun NotificationSettingsScreen(
    uiState: NotificationSettingsUiState,
    onEvent: (NotificationSettingsUiEvent) -> Unit,
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
            Text("Notificaciones", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) {
                Text("Volver")
            }
        }

        if (uiState.isLoading) {
            Text("Cargando configuracion de notificaciones...")
            return@Column
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Recordatorio diario")
                    Switch(
                        checked = uiState.isDailyReminderEnabled,
                        onCheckedChange = { onEvent(NotificationSettingsUiEvent.DailyReminderEnabledChanged(it)) },
                    )
                }

                OutlinedTextField(
                    value = uiState.reminderHour24.toString(),
                    onValueChange = { onEvent(NotificationSettingsUiEvent.ReminderHourChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Hora (0-23)") },
                )

                OutlinedTextField(
                    value = uiState.reminderMinute.toString(),
                    onValueChange = { onEvent(NotificationSettingsUiEvent.ReminderMinuteChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Minuto (0-59)") },
                )

                Button(
                    onClick = { onEvent(NotificationSettingsUiEvent.SaveClicked) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.isSaving) "Guardando..." else "Guardar notificaciones")
                }
            }
        }

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
