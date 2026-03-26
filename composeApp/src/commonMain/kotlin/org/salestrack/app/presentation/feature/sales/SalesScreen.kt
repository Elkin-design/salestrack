package org.salestrack.app.presentation.feature.sales

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

@Composable
fun SalesScreen(
    uiState: SalesUiState,
    onEvent: (SalesUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && event.key == Key.N) {
                    onEvent(SalesUiEvent.ToggleAddDialog(true))
                    true
                } else {
                    false
                }
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Ventas", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { onEvent(SalesUiEvent.ToggleAddDialog(true)) }) {
                Text("Nueva venta")
            }
        }

        OutlinedTextField(
            value = uiState.query,
            onValueChange = { onEvent(SalesUiEvent.QueryChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar por producto o vendedor") },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { onEvent(SalesUiEvent.CategoryChanged(null)) },
                label = { Text("Todas") },
            )
            uiState.availableCategories.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { onEvent(SalesUiEvent.CategoryChanged(category)) },
                    label = { Text(category) },
                )
            }
        }

        if (uiState.sales.isEmpty()) {
            Text(
                text = "No hay ventas para mostrar con los filtros actuales.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = sale.productName, style = MaterialTheme.typography.titleSmall)
            Text(text = "${sale.quantity} x $${formatMoney(sale.unitPrice)} · ${sale.category}")
            Text(
                text = "Neto: $${formatMoney(sale.netTotal)}",
                color = MaterialTheme.colorScheme.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDetail) { Text("Detalle") }
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) { Text("Eliminar") }
            }
        }
    }
}

@Composable
private fun SaleDetailDialog(sale: Sale, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle de venta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Producto: ${sale.productName}")
                Text("Categoría: ${sale.category}")
                Text("Cantidad: ${sale.quantity}")
                Text("Precio unitario: $${formatMoney(sale.unitPrice)}")
                Text("Descuento: $${formatMoney(sale.discount)}")
                Text("Total neto: $${formatMoney(sale.netTotal)}")
                Text("Vendedor: ${sale.sellerName}")
                Text("Timestamp UTC: ${sale.createdAtMillis}")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
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
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = product, onValueChange = { product = it }, label = { Text("Producto") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") })
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Cantidad") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") })
                OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("Descuento") })
                OutlinedTextField(value = seller, onValueChange = { seller = it }, label = { Text("Vendedor") })
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
    )
}


