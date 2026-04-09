package org.salestrack.app.presentation.feature.sales

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.presentation.app.AppContainer

@Composable
fun SalesRoute(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember {
        SalesViewModel(
            dispatcherProvider = container.dispatcherProvider,
            repository = container.saleRepository,
            addSaleUseCase = container.addSaleUseCase,
            updateSaleUseCase = container.updateSaleUseCase,
            deleteSaleUseCase = container.deleteSaleUseCase,
            filterSalesUseCase = container.filterSalesUseCase,
        )
    }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect {
            // Punto de integración para snackbar/toast multiplataforma.
        }
    }

    SalesScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    uiState: SalesUiState,
    onEvent: (SalesUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && event.key == Key.N) {
                    onEvent(SalesUiEvent.ToggleAddDialog(true))
                    true
                } else {
                    false
                }
            },
        topBar = {
            TopAppBar(
                title = { Text("Ventas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(SalesUiEvent.ToggleAddDialog(true)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Venta")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { onEvent(SalesUiEvent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por producto o vendedor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(100),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                )
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { onEvent(SalesUiEvent.CategoryChanged(null)) },
                        label = { Text("Todas") },
                        shape = RoundedCornerShape(100)
                    )
                }
                items(uiState.availableCategories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onEvent(SalesUiEvent.CategoryChanged(category)) },
                        label = { Text(category) },
                        shape = RoundedCornerShape(100)
                    )
                }
            }

            if (uiState.sales.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No hay ventas para mostrar.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.sales, key = { it.id }) { sale ->
                        SaleItem(
                            sale = sale,
                            onDetail = { onEvent(SalesUiEvent.ShowDetail(sale)) },
                            onEdit = { onEvent(SalesUiEvent.StartEdit(sale)) },
                            onDelete = { onEvent(SalesUiEvent.DeleteSale(sale.id)) },
                        )
                    }
                }
            }
        }
    }

    if (uiState.isAddDialogVisible) {
        SaleFormDialog(
            title = "Nueva venta",
            onDismiss = { onEvent(SalesUiEvent.ToggleAddDialog(false)) },
            onSave = { product, category, quantity, unitPrice, discount, seller ->
                onEvent(
                    SalesUiEvent.SaveNewSale(
                        productName = product,
                        category = category,
                        quantity = quantity,
                        unitPrice = unitPrice,
                        discount = discount,
                        seller = seller,
                    ),
                )
            },
        )
    }

    uiState.detailSale?.let { sale ->
        SaleDetailDialog(
            sale = sale,
            onDismiss = { onEvent(SalesUiEvent.ShowDetail(null)) },
        )
    }

    uiState.editingSale?.let { sale ->
        SaleFormDialog(
            title = "Editar venta",
            initialSale = sale,
            onDismiss = { onEvent(SalesUiEvent.StartEdit(null)) },
            onSave = { product, category, quantity, unitPrice, discount, seller ->
                onEvent(
                    SalesUiEvent.SaveEditedSale(
                        id = sale.id,
                        productName = product,
                        category = category,
                        quantity = quantity,
                        unitPrice = unitPrice,
                        discount = discount,
                        seller = seller,
                    ),
                )
            },
        )
    }
}

@Composable
private fun SaleItem(
    sale: Sale,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        text = sale.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(100),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = sale.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${sale.quantity} x $${formatMoney(sale.unitPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "$${formatMoney(sale.netTotal)}",
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
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sale.sellerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDetail) {
                        Icon(Icons.Default.Info, contentDescription = "Detalle", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleDetailDialog(sale: Sale, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Detalle de venta", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DetailRow(label = "Producto", value = sale.productName)
                DetailRow(label = "Categoría", value = sale.category)
                DetailRow(label = "Cantidad", value = sale.quantity.toString())
                DetailRow(label = "Precio unitario", value = "$${formatMoney(sale.unitPrice)}")
                DetailRow(label = "Descuento", value = "$${formatMoney(sale.discount)}")
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                DetailRow(
                    label = "Total neto",
                    value = "$${formatMoney(sale.netTotal)}",
                    valueStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                DetailRow(label = "Vendedor", value = sale.sellerName)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = valueStyle, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SaleFormDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Double, Double, String) -> Unit,
    initialSale: Sale? = null,
) {
    var product by remember(initialSale) { mutableStateOf(initialSale?.productName ?: "") }
    var category by remember(initialSale) { mutableStateOf(initialSale?.category ?: "General") }
    var quantity by remember(initialSale) { mutableStateOf((initialSale?.quantity ?: 1).toString()) }
    var price by remember(initialSale) { mutableStateOf((initialSale?.unitPrice ?: 0.0).toString()) }
    var discount by remember(initialSale) { mutableStateOf((initialSale?.discount ?: 0.0).toString()) }
    var seller by remember(initialSale) { mutableStateOf(initialSale?.sellerName ?: "Vendedor") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text("Producto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio Unit.") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Descuento") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = seller,
                    onValueChange = { seller = it },
                    label = { Text("Vendedor") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        product,
                        category,
                        quantity.toIntOrNull() ?: 0,
                        price.toDoubleOrNull() ?: 0.0,
                        discount.toDoubleOrNull() ?: 0.0,
                        seller,
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )
}
