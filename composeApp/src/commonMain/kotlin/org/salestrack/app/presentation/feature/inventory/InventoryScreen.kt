package org.salestrack.app.presentation.feature.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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

@Composable
fun InventoryScreen(
    uiState: InventoryUiState,
    onEvent: (InventoryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedProduct = uiState.products.firstOrNull { it.id == uiState.selectedProductId }
    val lowStockCount = uiState.lowStockProducts.size
    val totalUnits = uiState.products.sumOf { it.stock }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && event.key == Key.N) {
                    onEvent(InventoryUiEvent.ToggleAddDialog(true))
                    true
                } else {
                    false
                }
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Inventario", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { onEvent(InventoryUiEvent.ToggleAddDialog(true)) }) {
                Text("Nuevo producto")
            }
        }

        InventorySectionSelector(
            selectedSection = uiState.selectedSection,
            onSectionSelected = { onEvent(InventoryUiEvent.SectionChanged(it)) },
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = { onEvent(InventoryUiEvent.QueryChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar por nombre, categoria o codigo") },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { onEvent(InventoryUiEvent.CategoryChanged(null)) },
                label = { Text("Todas") },
            )
            uiState.availableCategories.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { onEvent(InventoryUiEvent.CategoryChanged(category)) },
                    label = { Text(category) },
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Resumen de inventario", style = MaterialTheme.typography.titleMedium)
                Text("Productos visibles: ${uiState.products.size}")
                Text("Unidades en stock: $totalUnits")
                Text("Productos con alerta de stock: $lowStockCount")
            }
        }

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

        ProductDetailCard(
            selectedProduct = selectedProduct,
            movements = uiState.selectedProductMovements,
        )

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
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
private fun InventorySectionSelector(
    selectedSection: InventorySection,
    onSectionSelected: (InventorySection) -> Unit,
) {
    val sections = listOf(
        InventorySection.Catalog to "Catalogo",
        InventorySection.AddProduct to "Agregar",
        InventorySection.EditProduct to "Editar",
        InventorySection.StockAdjustment to "Ajustes",
        InventorySection.MovementHistory to "Historial",
        InventorySection.ImportExport to "Importar/Exportar",
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sections) { (section, label) ->
            FilterChip(
                selected = selectedSection == section,
                onClick = { onSectionSelected(section) },
                label = { Text(label) },
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
        Text("No hay productos para mostrar con los filtros actuales.")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Ventana de alta de producto", style = MaterialTheme.typography.titleMedium)
            Text("Completa nombre, descripcion, precio, unidad, codigo, categoria, stock inicial y umbral.")
            Button(onClick = onOpenAddDialog) {
                Text("Abrir formulario de alta")
            }
            if (lowStockProducts.isNotEmpty()) {
                Text("Productos en riesgo de quiebre:")
                lowStockProducts.take(5).forEach { product ->
                    Text("- ${product.name}: ${product.stock}/${product.minimumStock}")
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Ventana de edicion", style = MaterialTheme.typography.titleMedium)
            Text("Selecciona un producto para editar sus campos.")
            products.take(8).forEach { product ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${product.name} · ${product.category}")
                    TextButton(onClick = { onEdit(product) }) { Text("Editar") }
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Ventana de ajuste de stock", style = MaterialTheme.typography.titleMedium)
            selectedProduct?.let {
                Text("Producto seleccionado: ${it.name}")
                Text("Stock actual: ${it.stock}")
                Button(onClick = { onAdjust(it) }) { Text("Abrir ajuste") }
            } ?: Text("Selecciona un producto desde catalogo para ajustar stock.")

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products, key = { it.id }) { product ->
                    FilterChip(
                        selected = selectedProduct?.id == product.id,
                        onClick = { onSelect(product.id) },
                        label = { Text(product.name) },
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Historial de movimientos", style = MaterialTheme.typography.titleMedium)
            Text("Producto: ${selectedProduct?.name ?: "Sin seleccion"}")
            if (movements.isEmpty()) {
                Text("Sin movimientos registrados")
            } else {
                movements.take(10).forEach { movement ->
                    Text("${movement.type.asLabel()} (${movement.quantityDelta}) · ${movement.reason}")
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Importar / Exportar catalogo", style = MaterialTheme.typography.titleMedium)
            Text("Formato CSV: name,description,unitPrice,unit,barcode,category,stock,minimumStock")
            OutlinedTextField(
                value = csvInput,
                onValueChange = onCsvInputChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pega aqui el CSV") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onImport) { Text("Importar CSV") }
                TextButton(onClick = onClearImport) { Text("Limpiar resultado") }
            }

            if (importResultSummary != null) {
                Text(importResultSummary)
                importErrors.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExportCsv) { Text("Exportar CSV") }
                Button(onClick = onExportExcel) { Text("Exportar Excel") }
                TextButton(onClick = onClearExport) { Text("Limpiar export") }
            }

            if (csvExportPreview != null) {
                Text("Preview CSV:")
                Text(csvExportPreview)
            }
            if (excelExportPreview != null) {
                Text("Preview Excel:")
                Text(excelExportPreview)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        onClick = onSelect,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleSmall)
            Text("Categoria: ${product.category} · Unidad: ${product.unit}")
            Text("Precio: $${formatMoney(product.unitPrice)}")
            Text("Stock: ${product.stock} · Umbral: ${product.minimumStock}")
            if (isLowStock) {
                Text("Stock bajo", color = MaterialTheme.colorScheme.error)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onAdjust) { Text("Ajustar") }
            }
        }
    }
}

@Composable
private fun ProductDetailCard(
    selectedProduct: Product?,
    movements: List<StockMovement>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Detalle del producto", style = MaterialTheme.typography.titleMedium)
            if (selectedProduct == null) {
                Text("Selecciona un producto para ver movimientos.")
                return@Column
            }

            Text(selectedProduct.name, style = MaterialTheme.typography.titleSmall)
            Text(selectedProduct.description)
            Text("Codigo: ${selectedProduct.barcode ?: "N/A"}")
            Text("Stock actual: ${selectedProduct.stock}")
            Text("Movimientos recientes")

            if (movements.isEmpty()) {
                Text("Sin movimientos registrados")
            } else {
                movements.take(5).forEach { movement ->
                    Text("${movement.type.asLabel()} (${movement.quantityDelta}) · ${movement.reason}")
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
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripcion") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") })
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unidad") })
                OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Codigo de barras (opcional)") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoria") })
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") })
                OutlinedTextField(value = minimum, onValueChange = { minimum = it }, label = { Text("Umbral minimo") })
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
        title = { Text("Ajustar stock · ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = delta,
                    onValueChange = { delta = it },
                    label = { Text("Cantidad (+/-)") },
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        StockAdjustmentType.Entry,
                        StockAdjustmentType.PhysicalCount,
                        StockAdjustmentType.Loss,
                    ).forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option.asLabel()) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(
                        delta.toIntOrNull() ?: 0,
                        reason,
                        type,
                    )
                },
            ) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

private fun StockAdjustmentType.asLabel(): String = when (this) {
    StockAdjustmentType.Entry -> "Entrada"
    StockAdjustmentType.PhysicalCount -> "Inventario fisico"
    StockAdjustmentType.Loss -> "Perdida"
    StockAdjustmentType.Sale -> "Venta"
    StockAdjustmentType.Return -> "Devolucion"
}
