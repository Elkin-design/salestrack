package org.salestrack.app.domain.usecase.inventory

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.repository.InventoryRepository

class GetLowStockProductsUseCase(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(): AppResult<List<Product>> = repository.getLowStockProducts()
}
