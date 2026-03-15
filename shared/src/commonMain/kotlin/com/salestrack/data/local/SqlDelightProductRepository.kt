package com.salestrack.data.local

import com.salestrack.db.SalesTrackDatabase
import com.salestrack.domain.model.Product
import com.salestrack.domain.repository.ProductRepository
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class SqlDelightProductRepository(
    private val database: SalesTrackDatabase
) : ProductRepository {
    private val queries = database.salesTrackDatabaseQueries

    override fun getProducts(): Flow<List<Product>> {
        return queries.selectAllProducts { id, name, description, price, unitOfMeasure, barcode, categoryId, stock, minStockThreshold ->
            Product(id, name, description, price, unitOfMeasure, barcode, categoryId, stock, minStockThreshold)
        }.asFlow().mapToList(Dispatchers.Default)
    }

    override suspend fun addProduct(product: Product) {
        queries.insertProduct(
            product.id,
            product.name,
            product.description,
            product.price,
            product.unitOfMeasure,
            product.barcode,
            product.categoryId,
            product.stock,
            product.minStockThreshold
        )
    }

    override suspend fun addProducts(products: List<Product>) {
        queries.transaction {
            products.forEach { product ->
                queries.insertProduct(
                    product.id,
                    product.name,
                    product.description,
                    product.price,
                    product.unitOfMeasure,
                    product.barcode,
                    product.categoryId,
                    product.stock,
                    product.minStockThreshold
                )
            }
        }
    }

    override suspend fun updateProduct(product: Product) {
        addProduct(product) // SQLDelight INSERT OR REPLACE
    }

    override suspend fun deleteProduct(productId: String) {
        // Soft delete or hard delete depending on needs. PDF mentions soft-delete for Firestore.
        // For local products, we might do hard delete or mark as deleted.
    }

    override suspend fun updateStock(productId: String, newStock: Int) {
        queries.updateStock(newStock, productId)
    }
}
