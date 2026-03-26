package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.SaleRepository

class FakeSaleRepository(
    private val dataSource: InMemorySaleDataSource,
    private val timeProvider: TimeProvider,
) : SaleRepository {

    override fun observeSales(): Flow<List<Sale>> = dataSource.observe()

    override suspend fun addSale(input: NewSaleInput): AppResult<Sale> {
        val sale = Sale(
            id = "S-${timeProvider.nowMillis()}",
            productName = input.productName,
            category = input.category,
            quantity = input.quantity,
            unitPrice = input.unitPrice,
            discount = input.discount,
            createdAtMillis = timeProvider.nowMillis(),
            sellerName = input.sellerName,
        )
        dataSource.replaceAll(dataSource.getCurrent() + sale)
        return AppResult.Success(sale)
    }

    override suspend fun updateSale(sale: Sale): AppResult<Sale> {
        val current = dataSource.getCurrent()
        val index = current.indexOfFirst { it.id == sale.id }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Venta no encontrada"))
        }
        val updated = current.toMutableList().apply { set(index, sale) }
        dataSource.replaceAll(updated)
        return AppResult.Success(sale)
    }

    override suspend fun softDeleteSale(saleId: String): AppResult<Unit> {
        val current = dataSource.getCurrent()
        val index = current.indexOfFirst { it.id == saleId }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Venta no encontrada"))
        }
        val sale = current[index]
        val updated = current.toMutableList().apply { set(index, sale.copy(isDeleted = true)) }
        dataSource.replaceAll(updated)
        return AppResult.Success(Unit)
    }
}

