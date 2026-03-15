package com.salestrack.data.sync

import com.salestrack.data.remote.FirebaseSalesDataSource
import com.salestrack.db.SalesTrackDatabase
import com.salestrack.domain.model.Sale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncManager(
    private val database: SalesTrackDatabase,
    private val remoteDataSource: FirebaseSalesDataSource,
    private val businessId: String // This should be dynamic based on current user
) {
    private val queries = database.salesTrackDatabaseQueries
    private val syncScope = CoroutineScope(Dispatchers.Default)

    fun startSync() {
        syncScope.launch {
            // Check for unsynced sales
            val unsyncedSalesEntities = queries.selectUnsyncedSales().executeAsList()
            unsyncedSalesEntities.forEach { entity ->
                val sale = Sale(
                    entity.id, entity.productName, entity.productId, entity.quantity,
                    entity.unitPrice, entity.discount, entity.totalAmount, entity.categoryId,
                    entity.vendorId, entity.platform, entity.timestamp, entity.isDeleted
                )
                try {
                    remoteDataSource.uploadSale(businessId, sale)
                    queries.markSaleSynced(entity.id)
                } catch (e: Exception) {
                    // Log error or retry later
                }
            }
        }
    }
}
