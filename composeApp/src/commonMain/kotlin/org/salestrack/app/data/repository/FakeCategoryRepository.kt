package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.data.source.InMemoryCategoryDataSource
import org.salestrack.app.domain.model.Category
import org.salestrack.app.domain.repository.CategoryRepository

class FakeCategoryRepository(
    private val dataSource: InMemoryCategoryDataSource,
    private val timeProvider: TimeProvider,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> = dataSource.observe()

    override suspend fun createCategory(name: String, colorHex: String): AppResult<Category> {
        val current = dataSource.getCurrent()
        if (current.any { it.name.equals(name, ignoreCase = true) && it.isActive }) {
            return AppResult.Failure(IllegalStateException("La categoria ya existe"))
        }

        val created = Category(
            id = "C-${timeProvider.nowMillis()}-${current.size + 1}",
            name = name,
            colorHex = colorHex,
            updatedAtMillis = timeProvider.nowMillis(),
        )

        dataSource.replaceAll((current + created).sortedBy { it.name.lowercase() })
        return AppResult.Success(created)
    }

    override suspend fun updateCategory(category: Category): AppResult<Category> {
        val current = dataSource.getCurrent()
        val index = current.indexOfFirst { it.id == category.id }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Categoria no encontrada"))
        }

        if (current.any { it.id != category.id && it.name.equals(category.name, ignoreCase = true) && it.isActive }) {
            return AppResult.Failure(IllegalStateException("Ya existe una categoria con ese nombre"))
        }

        val updated = category.copy(updatedAtMillis = timeProvider.nowMillis())
        val next = current.toMutableList().apply { set(index, updated) }
        dataSource.replaceAll(next.sortedBy { it.name.lowercase() })
        return AppResult.Success(updated)
    }

    override suspend fun deleteCategory(categoryId: String): AppResult<Unit> {
        val current = dataSource.getCurrent()
        val index = current.indexOfFirst { it.id == categoryId }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Categoria no encontrada"))
        }

        val target = current[index]
        val next = current.toMutableList().apply {
            set(index, target.copy(isActive = false, updatedAtMillis = timeProvider.nowMillis()))
        }
        dataSource.replaceAll(next.sortedBy { it.name.lowercase() })
        return AppResult.Success(Unit)
    }
}
