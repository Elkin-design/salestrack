package org.salestrack.app.domain.usecase.sales

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.SaleRepository

class AddSaleUseCase(
    private val repository: SaleRepository,
) {
    suspend operator fun invoke(input: NewSaleInput): AppResult<Sale> {
        if (input.productName.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El producto es obligatorio"))
        }
        if (input.quantity <= 0) {
            return AppResult.Failure(IllegalArgumentException("La cantidad debe ser mayor a 0"))
        }
        if (input.unitPrice <= 0.0) {
            return AppResult.Failure(IllegalArgumentException("El precio debe ser mayor a 0"))
        }
        if (input.discount < 0.0) {
            return AppResult.Failure(IllegalArgumentException("El descuento no puede ser negativo"))
        }
        return repository.addSale(input)
    }
}

