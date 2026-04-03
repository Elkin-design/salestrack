package org.salestrack.app.presentation.feature.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun CategoryManagementRoute(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember {
        CategoryManagementViewModel(
            dispatcherProvider = container.dispatcherProvider,
            observeCategoriesUseCase = container.observeCategoriesUseCase,
            createCategoryUseCase = container.createCategoryUseCase,
            updateCategoryUseCase = container.updateCategoryUseCase,
            deleteCategoryUseCase = container.deleteCategoryUseCase,
        )
    }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect {
            // Punto de extension para snackbar/toast.
        }
    }

    CategoryManagementScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun CategoryManagementScreen(
    uiState: CategoryManagementUiState,
    onEvent: (CategoryManagementUiEvent) -> Unit,
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
            Text("Categorias", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) {
                Text("Volver")
            }
        }

        if (uiState.isLoading) {
            Text("Cargando categorias...")
            return@Column
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Nueva categoria", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.newCategoryName,
                    onValueChange = { onEvent(CategoryManagementUiEvent.NewNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                )
                OutlinedTextField(
                    value = uiState.newCategoryColorHex,
                    onValueChange = { onEvent(CategoryManagementUiEvent.NewColorChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Color hex") },
                )
                Button(
                    onClick = { onEvent(CategoryManagementUiEvent.SaveNewCategory) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Crear categoria")
                }
            }
        }

        if (uiState.categories.isEmpty()) {
            Text("No hay categorias disponibles")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.categories, key = { it.id }) { category ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(category.name, style = MaterialTheme.typography.titleSmall)
                            Text("Color: ${category.colorHex}")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onEvent(CategoryManagementUiEvent.StartEdit(category)) }) {
                                    Text("Editar")
                                }
                                TextButton(onClick = { onEvent(CategoryManagementUiEvent.DeleteCategory(category.id)) }) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }

    if (uiState.editingCategory != null) {
        AlertDialog(
            onDismissRequest = { onEvent(CategoryManagementUiEvent.StartEdit(null)) },
            title = { Text("Editar categoria") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.editingName,
                        onValueChange = { onEvent(CategoryManagementUiEvent.EditNameChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre") },
                    )
                    OutlinedTextField(
                        value = uiState.editingColorHex,
                        onValueChange = { onEvent(CategoryManagementUiEvent.EditColorChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Color hex") },
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onEvent(CategoryManagementUiEvent.SaveEditedCategory) }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CategoryManagementUiEvent.StartEdit(null)) }) {
                    Text("Cancelar")
                }
            },
        )
    }
}
