package com.salestrack.data.local

import com.salestrack.db.SalesTrackDatabase
import com.salestrack.domain.model.Sale
import com.salestrack.domain.repository.SalesRepository
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.Flow

class SqlDelightSalesRepository(
    private val database: SalesTrackDatabase
) : SalesRepository {
    private val queries = database.salesTrackDatabaseQueries

    override fun getSales(): Flow<List<Sale>> {
        return queries.selectAllSales { id, productName, productId, quantity, unitPrice, discount, totalAmount, categoryId, vendorId, platform, timestamp, isDeleted, isSynced ->
            Sale(id, productName, productId, quantity, unitPrice, discount, totalAmount, categoryId, vendorId, platform, timestamp, isDeleted)
        }.asFlow().mapToList()
    }

    override suspend fun addSale(sale: Sale) {
        queries.insertSale(
            sale.id,
            sale.productName,
            sale.productId,
            sale.quantity,
            sale.unitPrice,
            sale.discount,
            sale.totalAmount,
            sale.categoryId,
            sale.vendorId,
            sale.platform,
            sale.timestamp,
            sale.isDeleted,
            false // isSynced
        )
    }

    override suspend fun updateSale(sale: Sale) {
        addSale(sale) // INSERT OR REPLACE
    }

    override suspend fun deleteSale(saleId: String) {
        // Implement soft delete local query if needed, or update isDeleted = 1
    }

    override suspend fun syncSales() {
        // This will be handled by the SyncManager together with a RemoteDataSource
    }
}
