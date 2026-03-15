package com.salestrack.presentation.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salestrack.domain.model.Sale
import com.salestrack.presentation.viewmodel.SalesViewModel
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesRegistrationScreen(
    viewModel: SalesViewModel,
    onBack: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Registrar Venta") },
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
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Producto") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) price = it },
                label = { Text("Precio Unitario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val q = quantity.toIntOrNull() ?: 1
                    val p = price.toDoubleOrNull() ?: 0.0
                    val sale = Sale(
                        id = Clock.System.now().toEpochMilliseconds().toString(),
                        productName = productName,
                        quantity = q,
                        unitPrice = p,
                        totalAmount = q * p,
                        categoryId = "default",
                        vendorId = "current_user",
                        platform = "common", // We should detect this
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                    viewModel.addSale(sale)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = productName.isNotBlank() && price.isNotBlank()
            ) {
                Text("Guardar Venta")
            }
        }
    }
}
