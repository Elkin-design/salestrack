package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import org.salestrack.app.domain.model.NewProductInput
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement
import org.salestrack.app.domain.repository.InventoryRepository

class FakeInventoryRepository(
    private val dataSource: InMemoryInventoryDataSource,
    private val timeProvider: TimeProvider,
) : InventoryRepository {

    override fun observeProducts(): Flow<List<Product>> = dataSource.observeProducts()

    override fun observeStockMovements(productId: String?): Flow<List<StockMovement>> {
        return dataSource.observeMovements().map { movements ->
            movements
                .asSequence()
                .filter { productId == null || it.productId == productId }
                .sortedByDescending { it.createdAtMillis }
                .toList()
        }
    }

    override suspend fun addProduct(input: NewProductInput): AppResult<Product> {
        val currentProducts = dataSource.getCurrentProducts()
        if (input.barcode != null && currentProducts.any { it.barcode == input.barcode }) {
            return AppResult.Failure(IllegalStateException("El codigo de barras ya existe"))
        }

        val product = Product(
            id = "P-${timeProvider.nowMillis()}-${currentProducts.size + 1}",
            name = input.name,
            description = input.description,
            unitPrice = input.unitPrice,
            unit = input.unit,
            barcode = input.barcode,
            category = input.category,
            stock = input.initialStock,
            minimumStock = input.minimumStock,
        )

        dataSource.replaceProducts(currentProducts + product)
        dataSource.appendMovement(
            StockMovement(
                id = "M-${timeProvider.nowMillis()}",
                productId = product.id,
                type = StockAdjustmentType.Entry,
                quantityDelta = input.initialStock,
                reason = "Stock inicial",
                platform = "Seed",
                createdAtMillis = timeProvider.nowMillis(),
            ),
        )

        return AppResult.Success(product)
    }

    override suspend fun updateProduct(product: Product): AppResult<Product> {
        val currentProducts = dataSource.getCurrentProducts()
        val index = currentProducts.indexOfFirst { it.id == product.id }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
        }

        if (product.barcode != null && currentProducts.any { it.id != product.id && it.barcode == product.barcode }) {
            return AppResult.Failure(IllegalStateException("El codigo de barras ya existe"))
        }

        val updated = currentProducts.toMutableList().apply { set(index, product) }
        dataSource.replaceProducts(updated)
        return AppResult.Success(product)
    }

    override suspend fun adjustStock(
        productId: String,
        quantityDelta: Int,
        reason: String,
        type: StockAdjustmentType,
        sellerName: String?,
        platform: String?,
    ): AppResult<Product> {
        val currentProducts = dataSource.getCurrentProducts()
        val index = currentProducts.indexOfFirst { it.id == productId }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
        }

        val currentProduct = currentProducts[index]
        val newStock = (currentProduct.stock + quantityDelta).coerceAtLeast(0)
        val appliedDelta = newStock - currentProduct.stock
        val updatedProduct = currentProduct.copy(stock = newStock)

        val updatedProducts = currentProducts.toMutableList().apply { set(index, updatedProduct) }
        dataSource.replaceProducts(updatedProducts)

        dataSource.appendMovement(
            StockMovement(
                id = "M-${timeProvider.nowMillis()}-${productId}",
                productId = productId,
                type = type,
                quantityDelta = appliedDelta,
                reason = reason,
                sellerName = sellerName,
                platform = platform,
                createdAtMillis = timeProvider.nowMillis(),
            ),
        )

        return AppResult.Success(updatedProduct)
    }

    override suspend fun deductStock(
        productId: String,
        quantity: Int,
        reason: String,
        sellerName: String?,
        platform: String?,
    ): AppResult<Product> {
        val product = dataSource.getCurrentProducts().firstOrNull { it.id == productId }
            ?: return AppResult.Failure(NoSuchElementException("Producto no encontrado"))

        if (product.stock < quantity) {
            return AppResult.Failure(IllegalStateException("Stock insuficiente"))
        }

        return adjustStock(
            productId = productId,
            quantityDelta = -quantity,
            reason = reason,
            type = StockAdjustmentType.Sale,
            sellerName = sellerName,
            platform = platform,
        )
    }
}

