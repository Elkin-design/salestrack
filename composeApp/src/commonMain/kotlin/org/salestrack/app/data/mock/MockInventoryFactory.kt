package org.salestrack.app.data.mock

import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement

object MockInventoryFactory {
    fun create(): List<Product> = listOf(
        Product(
            id = "P-1",
            name = "Cafe Premium",
            description = "Cafe tostado molido",
            unitPrice = 18_000.0,
            unit = "Bolsa",
            barcode = "770000000001",
            category = "Bebidas",
            stock = 12,
            minimumStock = 5,
        ),
        Product(
            id = "P-2",
            name = "Galletas Integrales",
            description = "Paquete x 12 unidades",
            unitPrice = 8_500.0,
            unit = "Paquete",
            barcode = "770000000002",
            category = "Snacks",
            stock = 4,
            minimumStock = 6,
        ),
        Product(
            id = "P-3",
            name = "Te Verde",
            description = "Caja por 20 sobres",
            unitPrice = 11_000.0,
            unit = "Caja",
            barcode = null,
            category = "Bebidas",
            stock = 20,
            minimumStock = 8,
        ),
        Product(
            id = "P-4",
            name = "Arroz Diana",
            description = "Bolsa de 1000g",
            unitPrice = 4_500.0,
            unit = "Bolsa",
            barcode = "770000000004",
            category = "Abarrotes",
            stock = 50,
            minimumStock = 10,
        ),
        Product(
            id = "P-5",
            name = "Leche Alqueria",
            description = "Bolsa de 1000ml",
            unitPrice = 3_800.0,
            unit = "Bolsa",
            barcode = "770000000005",
            category = "Lacteos",
            stock = 30,
            minimumStock = 10,
        ),
        Product(
            id = "P-6",
            name = "Pan Bimbo Blanco",
            description = "Paquete familiar",
            unitPrice = 6_500.0,
            unit = "Paquete",
            barcode = "770000000006",
            category = "Panaderia",
            stock = 15,
            minimumStock = 5,
        ),
        Product(
            id = "P-7",
            name = "Huevos Kikes AA",
            description = "Panal x 30 unidades",
            unitPrice = 16_000.0,
            unit = "Panal",
            barcode = "770000000007",
            category = "Lacteos y Huevos",
            stock = 25,
            minimumStock = 5,
        ),
    )

    fun createInitialMovements(
        timeProvider: TimeProvider,
        products: List<Product> = create(),
    ): List<StockMovement> {
        val now = timeProvider.nowMillis()
        return products.mapIndexed { index, product ->
            StockMovement(
                id = "M-INIT-${index + 1}",
                productId = product.id,
                type = StockAdjustmentType.Entry,
                quantityDelta = product.stock,
                reason = "Carga inicial de catalogo",
                platform = "Seed",
                createdAtMillis = now - (products.size - index) * 1_000L,
            )
        }
    }
}

