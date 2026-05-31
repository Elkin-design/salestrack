package org.salestrack.app.presentation.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
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
    var showAddDialog by remember { mutableStateOf(false) }

    val colorPresets = listOf(
        "#4F46E5", // Indigo
        "#0D9488", // Teal
        "#10B981", // Esmeralda
        "#3B82F6", // Azul
        "#EC4899", // Rosa
        "#F59E0B", // Ámbar
        "#F97316", // Naranja
        "#EF4444"  // Rojo
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Categorías", fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Añadir Categoría", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = 0.dp
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Tus Categorías",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp), // Espacio inferior para desplazar las tarjetas por encima del botón flotante
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

    // Ventana modal emergente para agregar nueva categoría (Se esconde al Guardar)
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        "Añadir Nueva Categoría",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.newCategoryName,
                        onValueChange = { onEvent(CategoryManagementUiEvent.NewNameChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre de categoría") },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                        )
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.newCategoryColorHex,
                            onValueChange = { onEvent(CategoryManagementUiEvent.NewColorChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Color Hex (ej: #4F46E5)") },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            ),
                            leadingIcon = {
                                val color = runCatching { Color(parseColor(uiState.newCategoryColorHex)) }.getOrDefault(Color.Gray)
                                Box(
                                    Modifier
                                        .size(24.dp)
                                        .background(color, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                                )
                            }
                        )

                        // Selector rápido de colores preestablecidos (Práctico y Elegante)
                        Text(
                            "Colores sugeridos:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                        ) {
                            colorPresets.forEach { presetHex ->
                                val presetColor = Color(parseColor(presetHex))
                                val isSelected = uiState.newCategoryColorHex.equals(presetHex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(presetColor)
                                        .clickable {
                                            onEvent(CategoryManagementUiEvent.NewColorChanged(presetHex))
                                        }
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEvent(CategoryManagementUiEvent.SaveNewCategory)
                        showAddDialog = false // Esconder ventana una vez se le dé a guardar
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Guardar Categoría", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (uiState.editingCategory != null) {
        AlertDialog(
            onDismissRequest = { onEvent(CategoryManagementUiEvent.StartEdit(null)) },
            title = { Text("Editar Categoría", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = uiState.editingName,
                        onValueChange = { onEvent(CategoryManagementUiEvent.EditNameChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                        )
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = uiState.editingColorHex,
                            onValueChange = { onEvent(CategoryManagementUiEvent.EditColorChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Color Hex") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            ),
                            leadingIcon = {
                                val color = runCatching { Color(parseColor(uiState.editingColorHex)) }.getOrDefault(Color.Gray)
                                Box(
                                    Modifier
                                        .size(20.dp)
                                        .background(color, CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                                )
                            }
                        )

                        // Selector rápido de edición
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colorPresets.forEach { presetHex ->
                                val presetColor = Color(parseColor(presetHex))
                                val isSelected = uiState.editingColorHex.equals(presetHex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(presetColor)
                                        .clickable {
                                            onEvent(CategoryManagementUiEvent.EditColorChanged(presetHex))
                                        }
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(CategoryManagementUiEvent.SaveEditedCategory) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(CategoryManagementUiEvent.StartEdit(null)) }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)), // Bordes coloreados elegantes según la categoría
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = color.copy(alpha = 0.15f), // Fondo suave coloreado
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color, CircleShape)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(colorHex, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.06f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
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
