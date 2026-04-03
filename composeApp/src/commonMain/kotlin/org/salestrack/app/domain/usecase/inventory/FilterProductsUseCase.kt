package org.salestrack.app.domain.usecase.inventory

import org.salestrack.app.domain.model.Product

class FilterProductsUseCase {
    operator fun invoke(
        products: List<Product>,
        query: String,
        category: String?,
    ): List<Product> {
        val normalizedQuery = query.trim().lowercase()

        return products
            .asSequence()
            .filter { it.isActive }
            .filter { category.isNullOrBlank() || it.category == category }
            .filter { product ->
                if (normalizedQuery.isBlank()) {
                    true
                } else {
                    product.name.lowercase().contains(normalizedQuery) ||
                        product.category.lowercase().contains(normalizedQuery) ||
                        (product.barcode?.contains(normalizedQuery) == true)
                }
            }
            .sortedBy { it.name }
            .toList()
    }
}

