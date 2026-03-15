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
            SmallTopAppBar(
                title = { Text("Catálogo de Productos") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                },
                actions = {
                    if (userRole != UserRole.VENDOR) {
                        IconButton(onClick = {
                            val csv = viewModel.exportCsv()
                            scope.launch {
                                snackbarHostState.showSnackbar("Catálogo exportado")
                            }
                        }) {
                            Text("Exp")
                        }
                        IconButton(onClick = {
                            val sampleCsv = """
                                Nombre,Descripción,Precio,Unidad,Stock,Umbral Mínimo,Código de Barras
                                "Producto Importado 1","Desc 1",10.5,"Und",100,10,"123456"
                            """.trimIndent()
                            viewModel.importCsv(sampleCsv)
                            scope.launch {
                                snackbarHostState.showSnackbar("Datos importados")
                            }
                        }) {
                            Text("Imp")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (userRole != UserRole.VENDOR) {
                FloatingActionButton(onClick = onNavigateToAddProduct) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
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
