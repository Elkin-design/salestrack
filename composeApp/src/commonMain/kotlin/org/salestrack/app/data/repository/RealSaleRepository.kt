package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.source.SaleDataSource
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.SaleRepository

class RealSaleRepository(
    private val dataSource: SaleDataSource,
) : SaleRepository {

    override fun observeSales(): Flow<List<Sale>> = dataSource.observeSales()

    override suspend fun addSale(input: NewSaleInput): AppResult<Sale> {
        val normalized = input.copy(
            sellerName = input.sellerName.ifBlank { "Sin vendedor" },
        )
        return dataSource.addSale(normalized)
    }

    override suspend fun updateSale(sale: Sale): AppResult<Sale> {
        return dataSource.updateSale(sale)
    }

    override suspend fun softDeleteSale(saleId: String): AppResult<Unit> {
        return dataSource.softDeleteSale(saleId)
    }

    override suspend fun clearAllSales(): AppResult<Unit> {
        return dataSource.clearAllSales()
    }
}
