package org.salestrack.app.domain.usecase.sales

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.repository.SaleRepository

import org.salestrack.app.domain.repository.CategoryRepository

class AddSaleUseCase(
    private val repository: SaleRepository,
    private val inventoryRepository: InventoryRepository? = null,
    private val categoryRepository: CategoryRepository? = null,
) {
    suspend operator fun invoke(input: NewSaleInput): AppResult<Sale> {
        if (input.items.isEmpty()) {
            return AppResult.Failure(IllegalArgumentException("El carrito no puede estar vacío"))
        }

        if (categoryRepository != null) {
            val activeCategories = categoryRepository.observeCategories().first()
            for (item in input.items) {
                val categoryExists = activeCategories.any { it.isActive && it.name.equals(item.category, ignoreCase = true) }
                if (!categoryExists) {
                    return AppResult.Failure(IllegalArgumentException("La categoría '${item.category}' del producto '${item.productName}' no es válida"))
                }
            }
        }
        
        for (item in input.items) {
            if (item.quantity <= 0) {
                return AppResult.Failure(IllegalArgumentException("La cantidad de '${item.productName}' debe ser mayor a 0"))
            }
            if (item.unitPrice <= 0.0) {
                return AppResult.Failure(IllegalArgumentException("El precio de '${item.productName}' debe ser mayor a 0"))
            }
            if (item.discount < 0.0) {
                return AppResult.Failure(IllegalArgumentException("El descuento de '${item.productName}' no puede ser negativo"))
            }
        }
        
        if (input.globalDiscount < 0.0) {
            return AppResult.Failure(IllegalArgumentException("El descuento global no puede ser negativo"))
        }

        val inventoryRepo = inventoryRepository
        if (inventoryRepo != null) {
            val products = inventoryRepo.observeProducts().first()
            for (item in input.items) {
                val matchingProduct = products.find { it.id == item.productId } ?: products.firstOrNull { it.isActive && it.name.equals(item.productName, ignoreCase = true) }
                
                if (matchingProduct != null) {
                    when (
                        val stockResult = inventoryRepo.deductStock(
                            productId = matchingProduct.id,
                            quantity = item.quantity,
                            reason = "Venta múltiple",
                            sellerName = input.sellerName,
                            platform = "App POS",
                        )
                    ) {
                        is AppResult.Success -> Unit
                        is AppResult.Failure -> return stockResult
                    }
                }
            }
        }

        return repository.addSale(input)
    }
}

