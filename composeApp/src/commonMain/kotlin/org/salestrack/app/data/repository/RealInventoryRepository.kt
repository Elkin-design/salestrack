package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.source.InventoryDataSource
import org.salestrack.app.domain.model.CatalogExportFile
import org.salestrack.app.domain.model.CatalogImportResult
import org.salestrack.app.domain.model.NewProductInput
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement
import org.salestrack.app.domain.repository.InventoryRepository

class RealInventoryRepository(
    private val dataSource: InventoryDataSource,
) : InventoryRepository {

    override fun observeProducts(): Flow<List<Product>> = dataSource.observeProducts()

    override fun observeStockMovements(productId: String?): Flow<List<StockMovement>> {
        return dataSource.observeStockMovements(productId)
    }

    override suspend fun addProduct(input: NewProductInput): AppResult<Product> {
        val normalized = input.copy(
            name = input.name.trim(),
            description = input.description.trim(),
            unit = input.unit.trim(),
            category = input.category.trim(),
            barcode = input.barcode?.trim()?.ifBlank { null },
        )
        return dataSource.addProduct(normalized)
    }

    override suspend fun updateProduct(product: Product): AppResult<Product> {
        val normalized = product.copy(
            name = product.name.trim(),
            description = product.description.trim(),
            unit = product.unit.trim(),
            category = product.category.trim(),
            barcode = product.barcode?.trim()?.ifBlank { null },
        )
        return dataSource.updateProduct(normalized)
    }

    override suspend fun adjustStock(
        productId: String,
        quantityDelta: Int,
        reason: String,
        type: StockAdjustmentType,
        sellerName: String?,
        platform: String?,
    ): AppResult<Product> {
        return dataSource.adjustStock(
            productId = productId,
            quantityDelta = quantityDelta,
            reason = reason,
            type = type,
            sellerName = sellerName,
            platform = platform,
        )
    }

    override suspend fun deductStock(
        productId: String,
        quantity: Int,
        reason: String,
        sellerName: String?,
        platform: String?,
    ): AppResult<Product> {
        return dataSource.deductStock(
            productId = productId,
            quantity = quantity,
            reason = reason,
            sellerName = sellerName,
            platform = platform,
        )
    }

    override suspend fun importCatalogCsv(csvContent: String): AppResult<CatalogImportResult> {
        return dataSource.importCatalogCsv(csvContent)
    }

    override suspend fun exportCatalogCsv(): AppResult<CatalogExportFile> = dataSource.exportCatalogCsv()

    override suspend fun exportCatalogExcel(): AppResult<CatalogExportFile> = dataSource.exportCatalogExcel()

    override suspend fun getLowStockProducts(): AppResult<List<Product>> = dataSource.getLowStockProducts()
}
