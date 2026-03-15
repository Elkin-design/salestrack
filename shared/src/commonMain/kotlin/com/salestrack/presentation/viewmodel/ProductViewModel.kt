package com.salestrack.presentation.viewmodel

import com.salestrack.domain.model.Product
import com.salestrack.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : BaseViewModel() {
    private val _productsState = MutableStateFlow<List<Product>>(emptyList())
    val productsState: StateFlow<List<Product>> = _productsState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            repository.getProducts().collect {
                _productsState.value = it
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.addProduct(product)
        }
    }

    fun updateStock(productId: String, quantityChange: Int) {
        viewModelScope.launch {
            val product = _productsState.value.find { it.id == productId }
            product?.let {
                val newStock = it.stock + quantityChange
                repository.updateStock(productId, newStock)
                
                // Logic for Alerta de stock bajo (RF-044) could be triggered here or in a UseCase
                if (newStock <= it.minStockThreshold) {
                    // Trigger alert notification
                }
            }
        }
    }

    fun importCsv(csvData: String) {
        viewModelScope.launch {
            val rows = com.salestrack.util.CsvParser.parse(csvData)
            if (rows.size <= 1) return@launch // Only header or empty

            val products = rows.drop(1).mapNotNull { row ->
                if (row.size < 5) return@mapNotNull null
                
                Product(
                    id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString() + "_" + row[0].hashCode(),
                    name = row[0],
                    description = row[1],
                    price = row[2].toDoubleOrNull() ?: 0.0,
                    unitOfMeasure = row[3],
                    barcode = if (row.size > 6) row[6] else null,
                    categoryId = "default",
                    stock = row[4].toIntOrNull() ?: 0,
                    minStockThreshold = row[5].toIntOrNull() ?: 5
                )
            }
            repository.addProducts(products)
        }
    }

    fun exportCsv(): String {
        val headers = listOf("Nombre", "Descripción", "Precio", "Unidad", "Stock", "Umbral Mínimo", "Código de Barras")
        val rows = _productsState.value.map { p ->
            listOf(
                p.name,
                p.description,
                p.price.toString(),
                p.unitOfMeasure,
                p.stock.toString(),
                p.minStockThreshold.toString(),
                p.barcode ?: ""
            )
        }
        return com.salestrack.util.CsvParser.toCsv(headers, rows)
    }
}
