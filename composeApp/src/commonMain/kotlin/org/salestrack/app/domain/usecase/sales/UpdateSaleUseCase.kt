package org.salestrack.app.domain.usecase.sales

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.SaleRepository

class UpdateSaleUseCase(
    private val repository: SaleRepository,
) {
    suspend operator fun invoke(sale: Sale): AppResult<Sale> {
        if (sale.productName.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El producto es obligatorio"))
        }
        if (sale.quantity <= 0 || sale.unitPrice <= 0.0) {
            return AppResult.Failure(IllegalArgumentException("Cantidad y precio deben ser mayores a 0"))
        }
        if (sale.discount < 0.0) {
            return AppResult.Failure(IllegalArgumentException("El descuento no puede ser negativo"))
        }
        return repository.updateSale(sale)
    }
}

