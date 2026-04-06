package org.salestrack.app.data.source

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale

interface SaleDataSource {
    fun observeSales(): Flow<List<Sale>>
    suspend fun addSale(input: NewSaleInput): AppResult<Sale>
    suspend fun updateSale(sale: Sale): AppResult<Sale>
    suspend fun softDeleteSale(saleId: String): AppResult<Unit>
}