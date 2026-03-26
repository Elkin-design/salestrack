package org.salestrack.app.data.mock

import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.Sale

object MockSalesFactory {
    fun create(timeProvider: TimeProvider): List<Sale> {
        val now = timeProvider.nowMillis()
        return listOf(
            Sale(
                id = "S-1001",
                productName = "Coca Cola 600ml",
                category = "Bebidas",
                quantity = 3,
                unitPrice = 3500.0,
                discount = 500.0,
                createdAtMillis = now - 30 * 60 * 1000,
                sellerName = "Ana",
            ),
            Sale(
                id = "S-1002",
                productName = "Papas BBQ",
                category = "Snacks",
                quantity = 2,
                unitPrice = 4200.0,
                discount = 0.0,
                createdAtMillis = now - 2 * 60 * 60 * 1000,
                sellerName = "Luis",
            ),
            Sale(
                id = "S-1003",
                productName = "Galletas Chocolate",
                category = "Snacks",
                quantity = 1,
                unitPrice = 5800.0,
                discount = 800.0,
                createdAtMillis = now - 26 * 60 * 60 * 1000,
                sellerName = "Ana",
            ),
        )
    }
}

