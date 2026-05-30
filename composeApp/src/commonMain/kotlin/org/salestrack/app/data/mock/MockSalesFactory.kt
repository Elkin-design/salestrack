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
                productName = "Arroz Diana",
                category = "Abarrotes",
                quantity = 5,
                unitPrice = 4500.0,
                discount = 0.0,
                createdAtMillis = now - 5L * 24 * 60 * 60 * 1000, // May
                sellerName = "Luis",
            ),
            Sale(
                id = "S-1003",
                productName = "Leche Alqueria",
                category = "Lacteos",
                quantity = 6,
                unitPrice = 3800.0,
                discount = 800.0,
                createdAtMillis = now - 15L * 24 * 60 * 60 * 1000, // May
                sellerName = "Ana",
            ),
            Sale(
                id = "S-1004",
                productName = "Pan Bimbo Blanco",
                category = "Panaderia",
                quantity = 2,
                unitPrice = 6500.0,
                discount = 0.0,
                createdAtMillis = now - 20L * 24 * 60 * 60 * 1000, // May
                sellerName = "Ana",
            ),
            Sale(
                id = "S-1005",
                productName = "Huevos Kikes AA",
                category = "Lacteos y Huevos",
                quantity = 1,
                unitPrice = 16000.0,
                discount = 0.0,
                createdAtMillis = now - 35L * 24 * 60 * 60 * 1000, // April
                sellerName = "Luis",
            ),
            Sale(
                id = "S-1006",
                productName = "Cafe Premium",
                category = "Bebidas",
                quantity = 2,
                unitPrice = 18000.0,
                discount = 2000.0,
                createdAtMillis = now - 40L * 24 * 60 * 60 * 1000, // April
                sellerName = "Ana",
            ),
            Sale(
                id = "S-1007",
                productName = "Arroz Diana",
                category = "Abarrotes",
                quantity = 10,
                unitPrice = 4500.0,
                discount = 5000.0,
                createdAtMillis = now - 45L * 24 * 60 * 60 * 1000, // April
                sellerName = "Luis",
            ),
            Sale(
                id = "S-1008",
                productName = "Galletas Integrales",
                category = "Snacks",
                quantity = 3,
                unitPrice = 8500.0,
                discount = 0.0,
                createdAtMillis = now - 50L * 24 * 60 * 60 * 1000, // April
                sellerName = "Ana",
            ),
            Sale(
                id = "S-1009",
                productName = "Leche Alqueria",
                category = "Lacteos",
                quantity = 12,
                unitPrice = 3800.0,
                discount = 1600.0,
                createdAtMillis = now - 55L * 24 * 60 * 60 * 1000, // April
                sellerName = "Luis",
            ),
        )
    }
}

