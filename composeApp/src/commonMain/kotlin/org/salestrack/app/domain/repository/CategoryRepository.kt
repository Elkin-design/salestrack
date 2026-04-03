package org.salestrack.app.domain.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Category

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun createCategory(name: String, colorHex: String): AppResult<Category>
    suspend fun updateCategory(category: Category): AppResult<Category>
    suspend fun deleteCategory(categoryId: String): AppResult<Unit>
}
