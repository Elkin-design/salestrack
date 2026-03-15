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
}
