package org.salestrack.app.domain.usecase.category

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.domain.model.Category
import org.salestrack.app.domain.repository.CategoryRepository

class ObserveCategoriesUseCase(
    private val repository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> = repository.observeCategories()
}
