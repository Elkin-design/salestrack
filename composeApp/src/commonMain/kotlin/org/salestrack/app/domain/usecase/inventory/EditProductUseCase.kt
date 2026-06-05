package org.salestrack.app.domain.usecase.inventory

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.repository.CategoryRepository

class EditProductUseCase(
    private val repository: InventoryRepository,
    private val categoryRepository: CategoryRepository,
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

        // Validate category exists in DB
        val activeCategories = try {
            categoryRepository.observeCategories().first()
        } catch (e: Exception) {
            emptyList()
        }
        val categoryExists = activeCategories.any { 
            it.name.equals(product.category, ignoreCase = true) && it.isActive 
        }
        if (!categoryExists) {
            return AppResult.Failure(IllegalArgumentException("La categoría '${product.category}' no existe. Debes crearla primero en Configuración."))
        }

        if (product.stock < 0 || product.minimumStock < 0) {
            return AppResult.Failure(IllegalArgumentException("Los valores de stock no pueden ser negativos"))
        }

        return repository.updateProduct(product)
    }
}

