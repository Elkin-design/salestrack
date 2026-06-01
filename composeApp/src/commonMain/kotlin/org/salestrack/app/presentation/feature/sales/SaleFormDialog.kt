package org.salestrack.app.presentation.feature.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.core.utils.formatMoney

@Composable
fun SaleFormDialog(
    title: String,
    inventoryProducts: List<Product>,
    isSaving: Boolean,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (product: String, category: String, quantity: Int, unitPrice: Double, discount: Double, seller: String, productId: String?) -> Unit,
    initialSale: Sale? = null,
) {
    var productQuery by remember(initialSale) { mutableStateOf(initialSale?.productName ?: "") }
    var selectedProductId by remember(initialSale) { mutableStateOf(initialSale?.productId) }
    var category by remember(initialSale) { mutableStateOf(initialSale?.category ?: "General") }
    var quantity by remember(initialSale) { mutableStateOf(initialSale?.quantity?.toString() ?: "") }
    var price by remember(initialSale) { mutableStateOf(if (initialSale != null && initialSale.unitPrice > 0.0) initialSale.unitPrice.toString() else "") }
    var discount by remember(initialSale) { mutableStateOf(if (initialSale != null && initialSale.discount > 0.0) initialSale.discount.toString() else "") }
    var seller by remember(initialSale) { mutableStateOf(initialSale?.sellerName ?: "Vendedor") }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            SaleFormContent(
                productQuery = productQuery,
                onProductQueryChange = { productQuery = it },
                selectedProductId = selectedProductId,
                onSelectedProductIdChange = { selectedProductId = it },
                category = category,
                onCategoryChange = { category = it },
                quantity = quantity,
                onQuantityChange = { quantity = it },
                price = price,
                onPriceChange = { price = it },
                discount = discount,
                onDiscountChange = { discount = it },
                seller = seller,
                onSellerChange = { seller = it },
                isSaving = isSaving,
                errorMessage = errorMessage,
                inventoryProducts = inventoryProducts
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        productQuery,
                        category,
                        quantity.toIntOrNull() ?: 1,
                        price.toDoubleOrNull() ?: 0.0,
                        discount.toDoubleOrNull() ?: 0.0,
                        seller,
                        selectedProductId
                    )
                },
                enabled = !isSaving && productQuery.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            ) {
                if (isSaving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text("Guardando...", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancelar", fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun SaleFormContent(
    productQuery: String,
    onProductQueryChange: (String) -> Unit,
    selectedProductId: String?,
    onSelectedProductIdChange: (String?) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    discount: String,
    onDiscountChange: (String) -> Unit,
    seller: String,
    onSellerChange: (String) -> Unit,
    isSaving: Boolean,
    errorMessage: String?,
    inventoryProducts: List<Product>
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredProducts = remember(productQuery, inventoryProducts) {
        if (productQuery.isBlank()) inventoryProducts 
        else inventoryProducts.filter { it.name.contains(productQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        OutlinedTextField(
            value = productQuery,
            enabled = !isSaving,
            onValueChange = { 
                onProductQueryChange(it)
                expanded = true
                if (selectedProductId != null) {
                    onSelectedProductIdChange(null)
                }
            },
            label = { Text("Producto") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)) },
            trailingIcon = {
                IconButton(
                    onClick = { expanded = !expanded },
                    enabled = !isSaving
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        )
        
        if (expanded && filteredProducts.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp), // Alto máximo para que sea scrollable y no ocupe todo el espacio
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                LazyColumn(
                    modifier = Modifier.padding(4.dp)
                ) {
                    items(filteredProducts) { prod ->
                        TextButton(
                            onClick = {
                                onProductQueryChange(prod.name)
                                onSelectedProductIdChange(prod.id)
                                onCategoryChange(prod.category)
                                onPriceChange(prod.unitPrice.toString())
                                expanded = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = prod.name,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = prod.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Stock: ${prod.stock}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (prod.stock < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = category,
            enabled = !isSaving,
            readOnly = true, // No permitir modificar la categoría
            onValueChange = onCategoryChange,
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = quantity,
                enabled = !isSaving,
                onValueChange = onQuantityChange,
                label = { Text("Cantidad") },
                placeholder = { Text("1") }, // Placeholder limpio
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            )
            OutlinedTextField(
                value = price,
                enabled = !isSaving,
                readOnly = true, // No permitir modificar el precio unitario
                onValueChange = onPriceChange,
                label = { Text("Precio Unit.") },
                placeholder = { Text("0.0") }, // Placeholder limpio
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            )
        }

        OutlinedTextField(
            value = discount,
            enabled = !isSaving,
            onValueChange = onDiscountChange,
            label = { Text("Descuento") },
            placeholder = { Text("0.0") }, // Placeholder limpio
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        )

        OutlinedTextField(
            value = seller,
            enabled = !isSaving,
            readOnly = true, // No permitir modificar el vendedor
            onValueChange = onSellerChange,
            label = { Text("Vendedor") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        )

        // Tarjeta de cálculo en tiempo real (Operación de venta y descuento)
        val q = quantity.toIntOrNull() ?: 1
        val p = price.toDoubleOrNull() ?: 0.0
        val d = discount.toDoubleOrNull() ?: 0.0
        val total = q * p
        val netTotal = maxOf(0.0, total - d)

        if (p > 0.0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subtotal (${q} x $${formatMoney(p)})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${formatMoney(total)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (d > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Descuento aplicado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "-$${formatMoney(d)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Neto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$${formatMoney(netTotal)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
