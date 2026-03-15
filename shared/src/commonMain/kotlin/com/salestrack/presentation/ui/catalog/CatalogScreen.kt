package com.salestrack.presentation.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salestrack.domain.model.Product
import com.salestrack.presentation.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.salestrack.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    userRole: UserRole,
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onNavigateToAddProduct: () -> Unit
) {
    val products by viewModel.productsState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Catálogo", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                },
                actions = {
                    if (userRole != UserRole.VENDOR) {
                        TextButton(onClick = {
                            viewModel.exportCsv()
                            scope.launch { snackbarHostState.showSnackbar("Exportado") }
                        }) { Text("Exportar") }
                        
                        TextButton(onClick = {
                            val sampleCsv = "Nombre,Precio\nProducto,10.0"
                            viewModel.importCsv(sampleCsv)
                            scope.launch { snackbarHostState.showSnackbar("Importado") }
                        }) { Text("Importar") }
                    }
                }
            )
        },
        floatingActionButton = {
            if (userRole != UserRole.VENDOR) {
                LargeFloatingActionButton(
                    onClick = onNavigateToAddProduct,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
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
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Stock: ${product.stock} ${product.unitOfMeasure}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            Text("$${product.price}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}
