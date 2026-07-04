package org.salestrack.app.domain.usecase.sales

import org.salestrack.app.domain.model.Sale

class FilterSalesUseCase {
    operator fun invoke(
        sales: List<Sale>,
        query: String,
        category: String?,
    ): List<Sale> {
        val queryNormalized = query.trim().lowercase()

        return sales
            .asSequence()
            .filter { !it.isDeleted }
            .filter {
                queryNormalized.isBlank() ||
                    it.productName.lowercase().contains(queryNormalized) ||
                    it.customerName.lowercase().contains(queryNormalized)
            }
            .filter { category.isNullOrBlank() || it.category == category }
            .sortedByDescending { it.createdAtMillis }
            .toList()
    }
}
