package org.salestrack.app.domain.usecase.inventory

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.repository.InventoryRepository

class EditProductUseCase(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(product: Product): AppResult<Product> {
        if (product.name.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El nombre del producto es obligatorio"))
        }
        if (product.unitPrice <= 0.0) {
            return AppResult.Failure(IllegalArgumentException("El precio debe ser mayor a 0"))
        }
        if (product.unit.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("La unidad de medida es obligatoria"))
        }
        if (product.category.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("La categoria es obligatoria"))
        }
        if (product.stock < 0 || product.minimumStock < 0) {
            return AppResult.Failure(IllegalArgumentException("Los valores de stock no pueden ser negativos"))
        }

        return repository.updateProduct(product)
    }
}

