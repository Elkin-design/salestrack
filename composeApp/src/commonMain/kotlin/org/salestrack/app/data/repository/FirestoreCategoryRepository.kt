package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.salestrack.app.core.firebase.FirebaseHelpers
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.Category
import org.salestrack.app.domain.repository.CategoryRepository
import dev.gitlive.firebase.firestore.Direction

class FirestoreCategoryRepository(
    private val timeProvider: TimeProvider,
) : CategoryRepository {

    private fun categoriesRef() = FirebaseHelpers.userRootDocument().collection("categories")

    override fun observeCategories(): Flow<List<Category>> {
        return categoriesRef()
            .snapshots
            .map { snap ->
                snap.documents
                    .mapNotNull { doc -> runCatching { doc.data<Category>() }.getOrNull() }
                    .filter { it.isActive }
                    .sortedBy { it.name.lowercase() }
            }
    }

    override suspend fun createCategory(name: String, colorHex: String): AppResult<Category> {
        return try {
            val snapshot = categoriesRef().get()
            val existing = snapshot.documents.mapNotNull { doc -> runCatching { doc.data<Category>() }.getOrNull() }
            
            if (existing.any { it.name.equals(name, ignoreCase = true) && it.isActive }) {
                return AppResult.Failure(IllegalStateException("La categoria ya existe"))
            }

            val now = timeProvider.nowMillis()
            val id = "C-$now-${existing.size + 1}"
            
            val category = Category(
                id = id,
                name = name,
                colorHex = colorHex,
                updatedAtMillis = now,
            )

            categoriesRef().document(id).set(category)
            AppResult.Success(category)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun updateCategory(category: Category): AppResult<Category> {
        return try {
            val snapshot = categoriesRef().get()
            val existing = snapshot.documents.mapNotNull { doc -> runCatching { doc.data<Category>() }.getOrNull() }
            
            if (existing.any { it.id != category.id && it.name.equals(category.name, ignoreCase = true) && it.isActive }) {
                return AppResult.Failure(IllegalStateException("Ya existe una categoria con ese nombre"))
            }

            val updated = category.copy(updatedAtMillis = timeProvider.nowMillis())
            categoriesRef().document(category.id).set(updated)
            
            AppResult.Success(updated)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: String): AppResult<Unit> {
        return try {
            val doc = categoriesRef().document(categoryId).get()
            if (!doc.exists) return AppResult.Failure(NoSuchElementException("Categoria no encontrada"))
            
            val category = runCatching { doc.data<Category>() }.getOrNull() 
                ?: return AppResult.Failure(IllegalStateException("Error al deserializar la categoria"))
            val updated = category.copy(isActive = false, updatedAtMillis = timeProvider.nowMillis())
            
            categoriesRef().document(categoryId).set(updated)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}
