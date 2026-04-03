package org.salestrack.app.domain.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.NewProductInput
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement

interface InventoryRepository {
    fun observeProducts(): Flow<List<Product>>
    fun observeStockMovements(productId: String? = null): Flow<List<StockMovement>>

    suspend fun addProduct(input: NewProductInput): AppResult<Product>
    suspend fun updateProduct(product: Product): AppResult<Product>

    suspend fun adjustStock(
        productId: String,
        quantityDelta: Int,
        reason: String,
        type: StockAdjustmentType,
        sellerName: String? = null,
        platform: String? = null,
    ): AppResult<Product>

    suspend fun deductStock(
        productId: String,
        quantity: Int,
        reason: String = "Venta",
        sellerName: String? = null,
        platform: String? = null,
    ): AppResult<Product>
}

