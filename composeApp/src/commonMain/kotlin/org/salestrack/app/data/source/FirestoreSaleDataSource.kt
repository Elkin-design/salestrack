package org.salestrack.app.data.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.salestrack.app.core.firebase.FirebaseHelpers
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale
import dev.gitlive.firebase.firestore.Direction

/**
 * Real Firestore implementation for SaleDataSource.
 */
class FirestoreSaleDataSource(
    private val timeProvider: TimeProvider,
) : SaleDataSource {

    private fun salesCollection() = FirebaseHelpers.userRootDocument().collection("sales")

    override fun observeSales(): Flow<List<Sale>> {
        return salesCollection()
            .orderBy("createdAtMillis", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<Sale>() }.filter { !it.isDeleted }
            }
    }

    override suspend fun addSale(input: NewSaleInput): AppResult<Sale> {
        return try {
            if (input.productName.isBlank()) {
                return AppResult.Failure(IllegalArgumentException("El producto es obligatorio"))
            }
            val now = timeProvider.nowMillis()
            val id = "FS-$now"
            val sale = Sale(
                id = id,
                productName = input.productName.trim(),
                category = input.category.trim().ifBlank { "General" },
                quantity = input.quantity,
                unitPrice = input.unitPrice,
                discount = input.discount,
                createdAtMillis = now,
                sellerName = input.sellerName.trim().ifBlank { "Sin vendedor" },
            )
            
            salesCollection().document(id).set(sale)
            AppResult.Success(sale)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun updateSale(sale: Sale): AppResult<Sale> {
        return try {
            salesCollection().document(sale.id).set(sale)
            AppResult.Success(sale)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun softDeleteSale(saleId: String): AppResult<Unit> {
        return try {
            salesCollection().document(saleId).update("isDeleted" to true)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}
