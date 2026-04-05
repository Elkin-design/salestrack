package org.salestrack.app.data.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale

/**
 * Placeholder data source to keep contracts ready before wiring Firebase Firestore.
 */
class FirestoreSaleDataSource {
    private val salesState = MutableStateFlow<List<Sale>>(emptyList())

    fun observeSales(): Flow<List<Sale>> = salesState.asStateFlow()

    suspend fun addSale(input: NewSaleInput): AppResult<Sale> {
        return AppResult.Failure(NotImplementedError("Firestore addSale pendiente"))
    }

    suspend fun updateSale(sale: Sale): AppResult<Sale> {
        return AppResult.Failure(NotImplementedError("Firestore updateSale pendiente"))
    }

    suspend fun softDeleteSale(saleId: String): AppResult<Unit> {
        return AppResult.Failure(NotImplementedError("Firestore softDeleteSale pendiente"))
    }
}
