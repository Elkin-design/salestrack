package org.salestrack.app.domain.usecase.sales

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.repository.SaleRepository

class AddSaleUseCase(
    private val repository: SaleRepository,
    private val inventoryRepository: InventoryRepository? = null,
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

        val inventoryRepo = inventoryRepository
        val matchingProduct = findMatchingProduct(input)
        if (inventoryRepo != null && matchingProduct != null) {
            when (
                val stockResult = inventoryRepo.deductStock(
                    productId = matchingProduct.id,
                    quantity = input.quantity,
                    reason = "Venta de ${input.productName}",
                    sellerName = input.sellerName,
                    platform = "App",
                )
            ) {
                is AppResult.Success -> Unit
                is AppResult.Failure -> return stockResult
            }
        }

        return repository.addSale(input)
    }

    private suspend fun findMatchingProduct(input: NewSaleInput): Product? {
        val inventoryRepo = inventoryRepository ?: return null
        return inventoryRepo
            .observeProducts()
            .first()
            .firstOrNull { product ->
                product.isActive && product.name.equals(input.productName, ignoreCase = true)
            }
    }
}

