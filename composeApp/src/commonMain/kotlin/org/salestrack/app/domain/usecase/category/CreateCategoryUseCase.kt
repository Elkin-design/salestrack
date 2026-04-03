package org.salestrack.app.domain.usecase.category

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Category
import org.salestrack.app.domain.repository.CategoryRepository

class CreateCategoryUseCase(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(name: String, colorHex: String): AppResult<Category> {
        if (name.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El nombre de categoria es obligatorio"))
        }
        if (!isValidHexColor(colorHex)) {
            return AppResult.Failure(IllegalArgumentException("El color debe ser hex valido, por ejemplo #1E88E5"))
        }
        return repository.createCategory(name.trim(), colorHex.uppercase())
    }

    private fun isValidHexColor(value: String): Boolean {
        return value.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }
}
