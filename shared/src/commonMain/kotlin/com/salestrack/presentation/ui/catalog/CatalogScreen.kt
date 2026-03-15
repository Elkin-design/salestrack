package com.salestrack.presentation.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salestrack.domain.model.Product
import com.salestrack.presentation.viewmodel.ProductViewModel

@Composable
fun CatalogScreen(
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onNavigateToAddProduct: () -> Unit
) {
    val products by viewModel.productsState.collectAsState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Catálogo de Productos") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddProduct) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                ProductItem(product)
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Text("Precio: $${product.price}", style = MaterialTheme.typography.bodyMedium)
            Text("Stock: ${product.stock} ${product.unitOfMeasure}", style = MaterialTheme.typography.bodySmall)
            
            if (product.stock <= product.minStockThreshold) {
                Text("STOCK BAJO", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
