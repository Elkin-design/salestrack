package org.salestrack.app.domain.usecase.inventory

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.repository.InventoryRepository

class AdjustStockUseCase(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(
        productId: String,
        quantityDelta: Int,
        reason: String,
        type: StockAdjustmentType,
        sellerName: String? = null,
        platform: String? = null,
    ): AppResult<Product> {
        if (productId.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El producto es obligatorio"))
        }
        if (quantityDelta == 0) {
            return AppResult.Failure(IllegalArgumentException("El ajuste no puede ser 0"))
        }
        if (reason.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El motivo del ajuste es obligatorio"))
        }

        return repository.adjustStock(
            productId = productId,
            quantityDelta = quantityDelta,
            reason = reason,
            type = type,
            sellerName = sellerName,
            platform = platform,
        )
    }
}

