package com.salestrack.presentation.viewmodel

import com.salestrack.domain.model.Product
import com.salestrack.domain.model.Sale
import com.salestrack.domain.repository.ProductRepository
import com.salestrack.domain.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesViewModel(
    private val repository: SalesRepository,
    private val productRepository: ProductRepository
) : BaseViewModel() {
    private val _salesState = MutableStateFlow<List<Sale>>(emptyList())
    val salesState: StateFlow<List<Sale>> = _salesState.asStateFlow()

    private val _scannedProduct = MutableStateFlow<Product?>(null)
    val scannedProduct: StateFlow<Product?> = _scannedProduct.asStateFlow()

    init {
        loadSales()
    }

    fun findProductByBarcode(barcode: String) {
        viewModelScope.launch {
            val product = productRepository.getProductByBarcode(barcode)
            _scannedProduct.value = product
        }
    }

    fun clearScannedProduct() {
        _scannedProduct.value = null
    }

    fun loadSales() {
        viewModelScope.launch {
            repository.getSales().collect {
                _salesState.value = it
            }
        }
    }

    fun addSale(sale: Sale) {
        viewModelScope.launch {
            repository.addSale(sale)
        }
    }
}