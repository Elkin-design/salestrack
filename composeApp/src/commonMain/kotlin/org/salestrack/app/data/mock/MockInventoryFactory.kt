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

