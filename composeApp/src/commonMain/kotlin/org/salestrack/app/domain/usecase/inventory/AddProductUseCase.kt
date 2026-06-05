package org.salestrack.app.domain.usecase.inventory

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.NewProductInput
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.repository.CategoryRepository

class AddProductUseCase(
    private val repository: InventoryRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(input: NewProductInput): AppResult<Product> {
        if (input.name.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El nombre del producto es obligatorio"))
        }
        if (input.unitPrice <= 0.0) {
            return AppResult.Failure(IllegalArgumentException("El precio debe ser mayor a 0"))
        }
        if (input.unit.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("La unidad de medida es obligatoria"))
        }
        if (input.category.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("La categoria es obligatoria"))
        }
        
        // Validate category exists in DB
        val activeCategories = try {
            categoryRepository.observeCategories().first()
        } catch (e: Exception) {
            emptyList()
        }
        val categoryExists = activeCategories.any { 
            it.name.equals(input.category, ignoreCase = true) && it.isActive 
        }
        if (!categoryExists) {
            return AppResult.Failure(IllegalArgumentException("La categoría '${input.category}' no existe. Debes crearla primero en Configuración."))
        }

        if (input.initialStock < 0) {
            return AppResult.Failure(IllegalArgumentException("El stock inicial no puede ser negativo"))
        }
        if (input.minimumStock < 0) {
            return AppResult.Failure(IllegalArgumentException("El umbral minimo no puede ser negativo"))
        }

        return repository.addProduct(input)
    }
}

