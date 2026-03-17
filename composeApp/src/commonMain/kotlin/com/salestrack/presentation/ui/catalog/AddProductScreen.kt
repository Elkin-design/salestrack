package com.salestrack.presentation.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salestrack.domain.model.Product
import com.salestrack.presentation.viewmodel.ProductViewModel
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Unidad") }
    var stock by remember { mutableStateOf("0") }
    var minStock by remember { mutableStateOf("5") }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Nuevo Producto") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Producto") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) price = it },
                    label = { Text("Precio") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("UDM") },
                    modifier = Modifier.weight(0.5f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = stock,
                    onValueChange = { if (it.all { char -> char.isDigit() }) stock = it },
                    label = { Text("Stock Inicial") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = minStock,
                    onValueChange = { if (it.all { char -> char.isDigit() }) minStock = it },
                    label = { Text("S. Mínimo") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val p = Product(
                        id = Clock.System.now().toEpochMilliseconds().toString(),
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        unitOfMeasure = unit,
                        stock = stock.toIntOrNull() ?: 0,
                        minStockThreshold = minStock.toIntOrNull() ?: 5,
                        categoryId = "default"
                    )
                    viewModel.addProduct(p)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && price.isNotBlank()
            ) {
                Text("Crear Producto")
            }
        }
    }
}
