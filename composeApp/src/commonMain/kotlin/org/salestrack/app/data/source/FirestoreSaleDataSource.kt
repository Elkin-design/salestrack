package org.salestrack.app.data.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale

/**
 * Firebase-ready stub that keeps repository contracts stable while backend is mocked.
 */
class FirestoreSaleDataSource(
    initialSales: List<Sale>,
    private val timeProvider: TimeProvider,
) : SaleDataSource {
    private val salesState = MutableStateFlow(initialSales)

    override fun observeSales(): Flow<List<Sale>> = salesState.asStateFlow()

    override suspend fun addSale(input: NewSaleInput): AppResult<Sale> {
        if (input.productName.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El producto es obligatorio"))
        }
        val now = timeProvider.nowMillis()
        val sale = Sale(
            id = "FS-$now",
            productName = input.productName.trim(),
            category = input.category.trim().ifBlank { "General" },
            quantity = input.quantity,
            unitPrice = input.unitPrice,
            discount = input.discount,
            createdAtMillis = now,
            sellerName = input.sellerName.trim().ifBlank { "Sin vendedor" },
        )
        salesState.value = salesState.value + sale
        return AppResult.Success(sale)
    }

    override suspend fun updateSale(sale: Sale): AppResult<Sale> {
        val current = salesState.value
        val index = current.indexOfFirst { it.id == sale.id && !it.isDeleted }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Venta no encontrada"))
        }
        val updated = current.toMutableList().apply { set(index, sale) }
        salesState.value = updated
        return AppResult.Success(sale)
    }

    override suspend fun softDeleteSale(saleId: String): AppResult<Unit> {
        val current = salesState.value
        val index = current.indexOfFirst { it.id == saleId }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Venta no encontrada"))
        }
        val updated = current.toMutableList().apply {
            set(index, current[index].copy(isDeleted = true))
        }
        salesState.value = updated
        return AppResult.Success(Unit)
    }
}
