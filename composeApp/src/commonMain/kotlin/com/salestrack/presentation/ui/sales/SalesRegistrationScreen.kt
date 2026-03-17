package com.salestrack.presentation.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.*
import com.salestrack.domain.model.Sale
import com.salestrack.presentation.viewmodel.SalesViewModel
import com.salestrack.util.BarcodeScanner
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesRegistrationScreen(
    viewModel: SalesViewModel,
    barcodeScanner: BarcodeScanner,
    onBack: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var barcodeBuffer by remember { mutableStateOf("") }

    val scannedProduct by viewModel.scannedProduct.collectAsState()

    LaunchedEffect(scannedProduct) {
        scannedProduct?.let {
            productName = it.name
            price = it.price.toString()
            viewModel.clearScannedProduct()
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Registrar Venta") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Atrás") }
                },
                actions = {
                    Button(onClick = {
                        barcodeScanner.startScan { result ->
                            result?.let { viewModel.findProductByBarcode(it) }
                        }
                    }) {
                        Text("Escanear")
                    }
                }
            )
        },
        modifier = Modifier.onKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown) {
                if (keyEvent.key == Key.Enter) {
                    if (barcodeBuffer.isNotEmpty()) {
                        viewModel.findProductByBarcode(barcodeBuffer)
                        barcodeBuffer = ""
                    }
                    true
                } else {
                    val char = keyEvent.utf16CodePoint.toChar()
                    if (char.isDigit() || char.isLetter()) {
                        barcodeBuffer += char
                    }
                    false
                }
            } else false
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
