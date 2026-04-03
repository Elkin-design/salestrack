package org.salestrack.app.data.source

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockMovement

class InMemoryInventoryDataSource(
    initialProducts: List<Product>,
    initialMovements: List<StockMovement> = emptyList(),
) {
    private val productsState = MutableStateFlow(initialProducts)
    private val movementsState = MutableStateFlow(initialMovements)

    fun observeProducts(): StateFlow<List<Product>> = productsState.asStateFlow()

    fun observeMovements(): StateFlow<List<StockMovement>> = movementsState.asStateFlow()

    fun getCurrentProducts(): List<Product> = productsState.value

    fun replaceProducts(products: List<Product>) {
        productsState.value = products
    }

    fun appendMovement(movement: StockMovement) {
        movementsState.value = movementsState.value + movement
    }
}

