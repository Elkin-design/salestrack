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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Delete
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
            deleteProductUseCase = container.deleteProductUseCase,
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
    val lowStockCount = uiState.lowStockProducts.size
    val totalUnits = uiState.products.sumOf { it.stock }
    
    val lazyListState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }
    var lastIndex by remember { mutableStateOf(0) }
    var lastOffset by remember { mutableStateOf(0) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
        val currentIndex = lazyListState.firstVisibleItemIndex
        val currentOffset = lazyListState.firstVisibleItemScrollOffset
        
        if (currentIndex > lastIndex) {
            isFabVisible = false
        } else if (currentIndex < lastIndex) {
            isFabVisible = true
        } else {
            if (currentOffset > lastOffset + 10) {
                isFabVisible = false
            } else if (currentOffset < lastOffset - 10) {
                isFabVisible = true
            }
        }
        lastIndex = currentIndex
        lastOffset = currentOffset
    }

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
            AnimatedVisibility(
                visible = isFabVisible,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = { onEvent(InventoryUiEvent.ToggleAddDialog(true)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Producto")
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { onEvent(InventoryUiEvent.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                placeholder = { Text("Buscar productos...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                )
            )


            InventorySummaryCard(
                totalProducts = uiState.products.size,
                totalUnits = totalUnits,
                lowStockCount = lowStockCount
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { onEvent(InventoryUiEvent.CategoryChanged(null)) },
                        label = { Text("Todas") },
                        leadingIcon = if (uiState.selectedCategory == null) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                items(uiState.availableCategories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onEvent(InventoryUiEvent.CategoryChanged(category)) },
                        label = { Text(category) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            CatalogWindow(
                products = uiState.products,
                selectedProductId = uiState.selectedProductId,
                onSelect = { onEvent(InventoryUiEvent.SelectProduct(it)) },
                onEdit = { onEvent(InventoryUiEvent.StartEdit(it)) },
                onAdjust = { onEvent(InventoryUiEvent.StartAdjust(it)) },
                onDelete = { onEvent(InventoryUiEvent.DeleteProduct(it)) },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                lazyListState = lazyListState
            )

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

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("¿Eliminar producto?", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar \"${product.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        onEvent(InventoryUiEvent.DeleteProduct(product.id))
                        productToDelete = null
                    },
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.error).run {
                        androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
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
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(
                label = "Catálogo",
                value = "$totalProducts",
                icon = Icons.AutoMirrored.Filled.List,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.height(32.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
            SummaryItem(
                label = "Unidades",
                value = "$totalUnits",
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.secondary
            )
            HorizontalDivider(modifier = Modifier.height(32.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
        Column {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
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
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
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
                onDelete = { onDelete(product.id) }
            )
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
    onDelete: () -> Unit,
) {
    val isLowStock = product.stock <= product.minimumStock

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        onClick = onSelect,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        product.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isLowStock) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Stock Bajo: ${product.stock}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "${product.stock} ${product.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${formatMoney(product.unitPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onAdjust, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Build, contentDescription = "Ajustar", tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
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
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
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
