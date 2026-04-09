package org.salestrack.app.presentation.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement
import org.salestrack.app.presentation.app.AppContainer

@Composable
fun InventoryRoute(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember {
        InventoryViewModel(
            dispatcherProvider = container.dispatcherProvider,
            repository = container.inventoryRepository,
            addProductUseCase = container.addProductUseCase,
            editProductUseCase = container.editProductUseCase,
            filterProductsUseCase = container.filterProductsUseCase,
            adjustStockUseCase = container.adjustStockUseCase,
            getLowStockProductsUseCase = container.getLowStockProductsUseCase,
            importCatalogCsvUseCase = container.importCatalogCsvUseCase,
            exportCatalogCsvUseCase = container.exportCatalogCsvUseCase,
            exportCatalogExcelUseCase = container.exportCatalogExcelUseCase,
        )
    }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect {
            // Punto de integración para snackbar/toast multiplataforma.
        }
    }

    InventoryScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    uiState: InventoryUiState,
    onEvent: (InventoryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedProduct = uiState.products.firstOrNull { it.id == uiState.selectedProductId }
    val lowStockCount = uiState.lowStockProducts.size
    val totalUnits = uiState.products.sumOf { it.stock }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && event.key == Key.N) {
                    onEvent(InventoryUiEvent.ToggleAddDialog(true))
                    true
                } else {
                    false
                }
            },
        topBar = {
            TopAppBar(
                title = { Text("Inventario", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(InventoryUiEvent.ToggleAddDialog(true)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Producto")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { onEvent(InventoryUiEvent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre, categoria o codigo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(100),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                )
            )

            InventorySectionSelector(
                selectedSection = uiState.selectedSection,
                onSectionSelected = { onEvent(InventoryUiEvent.SectionChanged(it)) },
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { onEvent(InventoryUiEvent.CategoryChanged(null)) },
                        label = { Text("Todas") },
                        shape = RoundedCornerShape(100)
                    )
                }
                items(uiState.availableCategories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onEvent(InventoryUiEvent.CategoryChanged(category)) },
                        label = { Text(category) },
                        shape = RoundedCornerShape(100)
                    )
                }
            }

            InventorySummaryCard(
                totalProducts = uiState.products.size,
                totalUnits = totalUnits,
                lowStockCount = lowStockCount
            )

            when (uiState.selectedSection) {
                InventorySection.Catalog -> CatalogWindow(
                    products = uiState.products,
                    selectedProductId = uiState.selectedProductId,
                    onSelect = { onEvent(InventoryUiEvent.SelectProduct(it)) },
                    onEdit = { onEvent(InventoryUiEvent.StartEdit(it)) },
                    onAdjust = { onEvent(InventoryUiEvent.StartAdjust(it)) },
                )
                InventorySection.AddProduct -> AddWindow(
                    lowStockProducts = uiState.lowStockProducts,
                    onOpenAddDialog = { onEvent(InventoryUiEvent.ToggleAddDialog(true)) },
                )
                InventorySection.EditProduct -> EditWindow(
                    products = uiState.products,
                    onEdit = { onEvent(InventoryUiEvent.StartEdit(it)) },
                )
                InventorySection.StockAdjustment -> StockAdjustmentWindow(
                    selectedProduct = selectedProduct,
                    onSelect = { onEvent(InventoryUiEvent.SelectProduct(it)) },
                    products = uiState.products,
                    onAdjust = { onEvent(InventoryUiEvent.StartAdjust(it)) },
                )
                InventorySection.MovementHistory -> HistoryWindow(
                    movements = uiState.selectedProductMovements,
                    selectedProduct = selectedProduct,
                )
                InventorySection.ImportExport -> ImportExportWindow(
                    csvInput = uiState.csvImportInput,
                    importResultSummary = uiState.importResult?.let {
                        "Filas: ${it.totalRows}, importadas: ${it.importedRows}, fallidas: ${it.failedRows}"
                    },
                    importErrors = uiState.importResult?.errors?.take(5)?.map { "Linea ${it.line}: ${it.reason}" }.orEmpty(),
                    csvExportPreview = uiState.lastCsvExport?.content?.lineSequence()?.take(3)?.joinToString("\n"),
                    excelExportPreview = uiState.lastExcelExport?.content?.take(80),
                    onCsvInputChanged = { onEvent(InventoryUiEvent.CsvImportInputChanged(it)) },
                    onImport = { onEvent(InventoryUiEvent.ImportCatalogFromCsv) },
                    onExportCsv = { onEvent(InventoryUiEvent.ExportCatalogAsCsv) },
                    onExportExcel = { onEvent(InventoryUiEvent.ExportCatalogAsExcel) },
                    onClearImport = { onEvent(InventoryUiEvent.ClearImportResult) },
                    onClearExport = { onEvent(InventoryUiEvent.ClearExportResult) },
                )
            }

            if (uiState.errorMessage != null) {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(uiState.errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }

    if (uiState.isAddDialogVisible) {
        ProductFormDialog(
            title = "Nuevo producto",
            onDismiss = { onEvent(InventoryUiEvent.ToggleAddDialog(false)) },
            onSave = { name, description, price, unit, barcode, category, stock, minimum ->
                onEvent(
                    InventoryUiEvent.SaveNewProduct(
                        name = name,
                        description = description,
                        unitPrice = price,
                        unit = unit,
                        barcode = barcode,
                        category = category,
                        initialStock = stock,
                        minimumStock = minimum,
                    ),
                )
            },
        )
    }

    uiState.editingProduct?.let { product ->
        ProductFormDialog(
            title = "Editar producto",
            initialProduct = product,
            onDismiss = { onEvent(InventoryUiEvent.StartEdit(null)) },
            onSave = { name, description, price, unit, barcode, category, stock, minimum ->
                onEvent(
                    InventoryUiEvent.SaveEditedProduct(
                        id = product.id,
                        name = name,
                        description = description,
                        unitPrice = price,
                        unit = unit,
                        barcode = barcode,
                        category = category,
                        stock = stock,
                        minimumStock = minimum,
                    ),
                )
            },
        )
    }

    uiState.adjustingProduct?.let { product ->
        StockAdjustmentDialog(
            product = product,
            onDismiss = { onEvent(InventoryUiEvent.StartAdjust(null)) },
            onApply = { delta, reason, type ->
                onEvent(
                    InventoryUiEvent.ApplyStockAdjustment(
                        productId = product.id,
                        quantityDelta = delta,
                        reason = reason,
                        type = type,
                    ),
                )
            },
        )
    }
}

@Composable
private fun InventorySummaryCard(totalProducts: Int, totalUnits: Int, lowStockCount: Int) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(
                label = "Catálogo",
                value = "$totalProducts",
                icon = Icons.Default.List,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SummaryItem(
                label = "Unidades",
                value = "$totalUnits",
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.secondary
            )
            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SummaryItem(
                label = "Alertas",
                value = "$lowStockCount",
                icon = Icons.Default.Warning,
                color = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
private fun InventorySectionSelector(
    selectedSection: InventorySection,
    onSectionSelected: (InventorySection) -> Unit,
) {
    val sections = listOf(
        InventorySection.Catalog to "Catálogo",
        InventorySection.AddProduct to "Agregar",
        InventorySection.EditProduct to "Editar",
        InventorySection.StockAdjustment to "Ajustes",
        InventorySection.MovementHistory to "Historial",
        InventorySection.ImportExport to "Datos",
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(sections) { (section, label) ->
            FilterChip(
                selected = selectedSection == section,
                onClick = { onSectionSelected(section) },
                label = { Text(label) },
                shape = RoundedCornerShape(100)
            )
        }
    }
}

@Composable
private fun CatalogWindow(
    products: List<Product>,
    selectedProductId: String?,
    onSelect: (String) -> Unit,
    onEdit: (Product) -> Unit,
    onAdjust: (Product) -> Unit,
) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "No hay productos para mostrar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                isSelected = product.id == selectedProductId,
                onSelect = { onSelect(product.id) },
                onEdit = { onEdit(product) },
                onAdjust = { onAdjust(product) },
            )
        }
    }
}

@Composable
private fun AddWindow(
    lowStockProducts: List<Product>,
    onOpenAddDialog: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Gestión de Inventario Inicial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Integra nuevos ingresos con control de stock y clasificación detallada.", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onOpenAddDialog, shape = RoundedCornerShape(100)) {
                Text("Abrir formulario de alta")
            }
            if (lowStockProducts.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Productos en riesgo", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
                lowStockProducts.take(5).forEach { product ->
                    Text("• ${product.name}: ${product.stock} (Min: ${product.minimumStock})", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun EditWindow(
    products: List<Product>,
    onEdit: (Product) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Edición rápida", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Selecciona un producto del catálogo para actualizar sus detalles generales.", style = MaterialTheme.typography.bodyMedium)
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            products.take(8).forEach { product ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${product.name}", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { onEdit(product) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun StockAdjustmentWindow(
    selectedProduct: Product?,
    onSelect: (String) -> Unit,
    products: List<Product>,
    onAdjust: (Product) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Ajustes de inventario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            selectedProduct?.let {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Producto a ajustar: ${it.name}", fontWeight = FontWeight.Bold)
                        Text("Stock actual: ${it.stock}", style = MaterialTheme.typography.bodyMedium)
                        Button(
                            onClick = { onAdjust(it) },
                            modifier = Modifier.padding(top = 8.dp),
                            shape = RoundedCornerShape(100)
                        ) {
                            Text("Registrar movimiento")
                        }
                    }
                }
            } ?: Text("Selecciona un producto en el catálogo o debajo para registrar mermas o reingresos.", style = MaterialTheme.typography.bodyMedium)

            Text("Productos disponibles:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products, key = { it.id }) { product ->
                    FilterChip(
                        selected = selectedProduct?.id == product.id,
                        onClick = { onSelect(product.id) },
                        label = { Text(product.name) },
                        shape = RoundedCornerShape(100)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryWindow(
    movements: List<StockMovement>,
    selectedProduct: Product?,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Kardex / Historial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Actividad para: ${selectedProduct?.name ?: "Sin selección"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            
            Divider()
            
            if (movements.isEmpty()) {
                Text("No hay registros en el historial.", style = MaterialTheme.typography.bodyMedium)
            } else {
                movements.take(10).forEach { movement ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(movement.type.asLabel(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(movement.reason.ifBlank { "Sin detalle" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = if (movement.quantityDelta > 0) "+${movement.quantityDelta}" else "${movement.quantityDelta}",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (movement.quantityDelta > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun ImportExportWindow(
    csvInput: String,
    importResultSummary: String?,
    importErrors: List<String>,
    csvExportPreview: String?,
    excelExportPreview: String?,
    onCsvInputChanged: (String) -> Unit,
    onImport: () -> Unit,
    onExportCsv: () -> Unit,
    onExportExcel: () -> Unit,
    onClearImport: () -> Unit,
    onClearExport: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Migración de Datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = csvInput,
                onValueChange = onCsvInputChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pega el CSV aquí") },
                shape = RoundedCornerShape(12.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onImport) { Text("Importar") }
                TextButton(onClick = onClearImport) { Text("Limpiar") }
            }

            if (importResultSummary != null) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(importResultSummary, fontWeight = FontWeight.Bold)
                        importErrors.forEach { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }

            Divider()
            Text("Descarga", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExportCsv) { Text("Exp. CSV") }
                Button(onClick = onExportExcel) { Text("Exp. Excel") }
            }
            if (csvExportPreview != null || excelExportPreview != null) {
                TextButton(onClick = onClearExport) { Text("Limpiar previsualización") }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onAdjust: () -> Unit,
) {
    val isLowStock = product.stock <= product.minimumStock

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        onClick = onSelect,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Unidad: ${product.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "$${formatMoney(product.unitPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLowStock) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stock Bajo: ${product.stock}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "Stock: ${product.stock} (Min: ${product.minimumStock})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onAdjust) {
                        Icon(Icons.Default.Build, contentDescription = "Ajustar", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductFormDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, String?, String, Int, Int) -> Unit,
    initialProduct: Product? = null,
) {
    var name by remember(initialProduct) { mutableStateOf(initialProduct?.name ?: "") }
    var description by remember(initialProduct) { mutableStateOf(initialProduct?.description ?: "") }
    var price by remember(initialProduct) { mutableStateOf((initialProduct?.unitPrice ?: 0.0).toString()) }
    var unit by remember(initialProduct) { mutableStateOf(initialProduct?.unit ?: "Unidad") }
    var barcode by remember(initialProduct) { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember(initialProduct) { mutableStateOf(initialProduct?.category ?: "General") }
    var stock by remember(initialProduct) { mutableStateOf((initialProduct?.stock ?: 0).toString()) }
    var minimum by remember(initialProduct) { mutableStateOf((initialProduct?.minimumStock ?: 0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidad") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    leadingIcon = { Icon(Icons.Default.List, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Código de barras (opcional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock Inicial") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = minimum,
                        onValueChange = { minimum = it },
                        label = { Text("Umbral Mín.") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        description,
                        price.toDoubleOrNull() ?: 0.0,
                        unit,
                        barcode.ifBlank { null },
                        category,
                        stock.toIntOrNull() ?: 0,
                        minimum.toIntOrNull() ?: 0,
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun StockAdjustmentDialog(
    product: Product,
    onDismiss: () -> Unit,
    onApply: (Int, String, StockAdjustmentType) -> Unit,
) {
    var delta by remember { mutableStateOf("0") }
    var reason by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(StockAdjustmentType.Entry) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Ajuste de Stock", fontWeight = FontWeight.Bold)
                Text(product.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = delta,
                    onValueChange = { delta = it },
                    label = { Text("Cantidad a sumar (+ o -)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo/Justificación") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tipo de ajuste:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(StockAdjustmentType.Entry, StockAdjustmentType.PhysicalCount, StockAdjustmentType.Loss)) { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option.asLabel()) },
                            shape = RoundedCornerShape(100)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(delta.toIntOrNull() ?: 0, reason, type) }
            ) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )
}

private fun StockAdjustmentType.asLabel(): String = when (this) {
    StockAdjustmentType.Entry -> "Entrada"
    StockAdjustmentType.PhysicalCount -> "Inventario Físico"
    StockAdjustmentType.Loss -> "Pérdida/Merna"
    StockAdjustmentType.Sale -> "Venta"
    StockAdjustmentType.Return -> "Devolución"
}
