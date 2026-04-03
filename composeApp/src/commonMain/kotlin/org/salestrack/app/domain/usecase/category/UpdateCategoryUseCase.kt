package org.salestrack.app.domain.usecase.category

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Category
import org.salestrack.app.domain.repository.CategoryRepository

class UpdateCategoryUseCase(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(category: Category): AppResult<Category> {
        if (category.id.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El id de categoria es obligatorio"))
        }
        if (category.name.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El nombre de categoria es obligatorio"))
        }
        if (!category.colorHex.matches(Regex("^#[0-9A-Fa-f]{6}$"))) {
            return AppResult.Failure(IllegalArgumentException("El color debe ser hex valido"))
        }
        return repository.updateCategory(category.copy(name = category.name.trim(), colorHex = category.colorHex.uppercase()))
    }
}
