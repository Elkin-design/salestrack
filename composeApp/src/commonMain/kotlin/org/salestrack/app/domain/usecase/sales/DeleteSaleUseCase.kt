package org.salestrack.app.domain.usecase.sales

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.repository.SaleRepository

class DeleteSaleUseCase(
    private val repository: SaleRepository,
) {
    suspend operator fun invoke(saleId: String): AppResult<Unit> {
        if (saleId.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("ID inválido"))
        }
        return repository.softDeleteSale(saleId)
    }
}

