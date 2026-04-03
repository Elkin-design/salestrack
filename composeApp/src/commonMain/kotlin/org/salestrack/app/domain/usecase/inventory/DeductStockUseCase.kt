package org.salestrack.app.domain.usecase.inventory

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.repository.InventoryRepository

class DeductStockUseCase(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(
        productId: String,
        quantity: Int,
        reason: String = "Venta",
        sellerName: String? = null,
        platform: String? = null,
    ): AppResult<Product> {
        if (productId.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El producto es obligatorio"))
        }
        if (quantity <= 0) {
            return AppResult.Failure(IllegalArgumentException("La cantidad debe ser mayor a 0"))
        }

        return repository.deductStock(
            productId = productId,
            quantity = quantity,
            reason = reason,
            sellerName = sellerName,
            platform = platform,
        )
    }
}

