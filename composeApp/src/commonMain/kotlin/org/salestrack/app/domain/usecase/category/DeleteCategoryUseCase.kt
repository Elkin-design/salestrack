package org.salestrack.app.domain.usecase.category

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.repository.CategoryRepository

class DeleteCategoryUseCase(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(categoryId: String): AppResult<Unit> {
        if (categoryId.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El id de categoria es obligatorio"))
        }
        return repository.deleteCategory(categoryId)
    }
}
