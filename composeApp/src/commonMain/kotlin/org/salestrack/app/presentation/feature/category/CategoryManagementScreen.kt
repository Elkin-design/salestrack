package org.salestrack.app.presentation.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            inventoryRepository = container.inventoryRepository,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    uiState: CategoryManagementUiState,
    onEvent: (CategoryManagementUiEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Categorías", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Sección de nueva categoría
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir Nueva Categoría", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    
                    OutlinedTextField(
                        value = uiState.newCategoryName,
                        onValueChange = { onEvent(CategoryManagementUiEvent.NewNameChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre de categoría") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = uiState.newCategoryColorHex,
                        onValueChange = { onEvent(CategoryManagementUiEvent.NewColorChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Color Hex (ej: #FF5733)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            val color = runCatching { Color(parseColor(uiState.newCategoryColorHex)) }.getOrDefault(Color.Gray)
                            Box(Modifier.size(20.dp).background(color, CircleShape))
                        }
                    )
                    
                    Button(
                        onClick = { onEvent(CategoryManagementUiEvent.SaveNewCategory) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Categoría", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                "Tus Categorías",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.categories.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No hay categorías configuradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryItem(
                            name = category.name,
                            colorHex = category.colorHex,
                            onEdit = { onEvent(CategoryManagementUiEvent.StartEdit(category)) },
                            onDelete = { onEvent(CategoryManagementUiEvent.DeleteCategory(category.id)) }
                        )
                    }
                }
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (uiState.editingCategory != null) {
        AlertDialog(
            onDismissRequest = { onEvent(CategoryManagementUiEvent.StartEdit(null)) },
            title = { Text("Editar Categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.editingName,
                        onValueChange = { onEvent(CategoryManagementUiEvent.EditNameChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.editingColorHex,
                        onValueChange = { onEvent(CategoryManagementUiEvent.EditColorChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Color Hex") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onEvent(CategoryManagementUiEvent.SaveEditedCategory) }) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CategoryManagementUiEvent.StartEdit(null)) }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun CategoryItem(
    name: String,
    colorHex: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = runCatching { Color(parseColor(colorHex)) }.getOrDefault(Color.Gray)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(colorHex, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// Función auxiliar para parsear color hex
private fun parseColor(hex: String): Long {
    return try {
        val cleanHex = hex.removePrefix("#")
        val finalHex = if (cleanHex.length == 6) "FF$cleanHex" else cleanHex
        finalHex.toLong(16)
    } catch (e: Exception) {
        0xFF808080 // Gris por defecto
    }
}
