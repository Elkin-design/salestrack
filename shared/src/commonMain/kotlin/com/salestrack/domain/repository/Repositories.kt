package com.salestrack.domain.repository

import com.salestrack.domain.model.Product
import com.salestrack.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SalesRepository {
    fun getSales(): Flow<List<Sale>>
    suspend fun addSale(sale: Sale)
    suspend fun updateSale(sale: Sale)
    suspend fun deleteSale(saleId: String)
    suspend fun syncSales()
}

interface ProductRepository {
    fun getProducts(): Flow<List<Product>>
    suspend fun addProduct(product: Product)
    suspend fun addProducts(products: List<Product>)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(productId: String)
    suspend fun updateStock(productId: String, newStock: Int)
}
