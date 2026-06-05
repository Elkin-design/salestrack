package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.salestrack.app.core.firebase.FirebaseHelpers
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.Category
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.CategoryRepository
import dev.gitlive.firebase.firestore.Direction

class FirestoreCategoryRepository(
    private val timeProvider: TimeProvider,
) : CategoryRepository {

    private var hasSeeded = false
    private val repositoryScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)

    private fun seedDefaultCategories() {
        repositoryScope.launch {
            try {
                val defaults = listOf(
                    Category(id = "C-GEN", name = "General", colorHex = "#9E9E9E", isActive = true, updatedAtMillis = timeProvider.nowMillis()),
                    Category(id = "C-ALI", name = "Alimentos", colorHex = "#FF9800", isActive = true, updatedAtMillis = timeProvider.nowMillis()),
                    Category(id = "C-BEB", name = "Bebidas", colorHex = "#2196F3", isActive = true, updatedAtMillis = timeProvider.nowMillis()),
                    Category(id = "C-LIM", name = "Limpieza", colorHex = "#4CAF50", isActive = true, updatedAtMillis = timeProvider.nowMillis()),
                    Category(id = "C-ELE", name = "Electrónica", colorHex = "#9C27B0", isActive = true, updatedAtMillis = timeProvider.nowMillis()),
                    Category(id = "C-ROP", name = "Ropa", colorHex = "#E91E63", isActive = true, updatedAtMillis = timeProvider.nowMillis()),
                    Category(id = "C-SAL", name = "Salud", colorHex = "#00BCD4", isActive = true, updatedAtMillis = timeProvider.nowMillis()),
                    Category(id = "C-HOG", name = "Hogar", colorHex = "#795548", isActive = true, updatedAtMillis = timeProvider.nowMillis()),
                    Category(id = "C-SER", name = "Servicios", colorHex = "#607D8B", isActive = true, updatedAtMillis = timeProvider.nowMillis())
                )
                for (cat in defaults) {
                    categoriesRef().document(cat.id).set(cat)
                }
            } catch (e: Exception) {
                // Ignore seeding errors
            }
        }
    }

    private fun categoriesRef() = FirebaseHelpers.userRootDocument().collection("categories")

    override fun observeCategories(): Flow<List<Category>> {
        return categoriesRef()
            .snapshots
            .map { snap ->
                val list = snap.documents
                    .mapNotNull { doc -> runCatching { doc.data<Category>() }.getOrNull() }
                    .filter { it.isActive }
                if (list.isEmpty() && !hasSeeded) {
                    hasSeeded = true
                    seedDefaultCategories()
                }
                list.sortedBy { it.name.lowercase() }
            }
            .catch { emit(emptyList()) }
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
            val oldDoc = categoriesRef().document(category.id).get()
            val oldName = if (oldDoc.exists) {
                runCatching { oldDoc.data<Category>().name }.getOrNull()
            } else null

            val snapshot = categoriesRef().get()
            val existing = snapshot.documents.mapNotNull { doc -> runCatching { doc.data<Category>() }.getOrNull() }
            
            if (existing.any { it.id != category.id && it.name.equals(category.name, ignoreCase = true) && it.isActive }) {
                return AppResult.Failure(IllegalStateException("Ya existe una categoria con ese nombre"))
            }

            val updated = category.copy(updatedAtMillis = timeProvider.nowMillis())
            categoriesRef().document(category.id).set(updated)

            if (oldName != null && !oldName.equals(category.name, ignoreCase = true)) {
                // Update products in inventory
                val productsRef = FirebaseHelpers.userRootDocument().collection("inventory")
                val productsSnap = productsRef.get()
                for (doc in productsSnap.documents) {
                    val product = runCatching { doc.data<Product>() }.getOrNull()
                    if (product != null && product.category.equals(oldName, ignoreCase = true)) {
                        productsRef.document(product.id).update(mapOf("category" to category.name))
                    }
                }

                // Update sales
                val salesRef = FirebaseHelpers.userRootDocument().collection("sales")
                val salesSnap = salesRef.get()
                for (doc in salesSnap.documents) {
                    val sale = runCatching { doc.data<Sale>() }.getOrNull()
                    if (sale != null && sale.category.equals(oldName, ignoreCase = true)) {
                        salesRef.document(sale.id).update(mapOf("category" to category.name))
                    }
                }
            }
            
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
