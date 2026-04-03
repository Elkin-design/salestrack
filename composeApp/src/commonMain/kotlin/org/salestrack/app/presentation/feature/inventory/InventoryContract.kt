package org.salestrack.app.presentation.feature.inventory

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement

data class InventoryUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategory: String? = null,
    val availableCategories: List<String> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedProductId: String? = null,
    val selectedProductMovements: List<StockMovement> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val editingProduct: Product? = null,
    val adjustingProduct: Product? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface InventoryUiEvent : UiEvent {
    data object Refresh : InventoryUiEvent
    data class QueryChanged(val value: String) : InventoryUiEvent
    data class CategoryChanged(val value: String?) : InventoryUiEvent
    data class SelectProduct(val productId: String?) : InventoryUiEvent
    data class ToggleAddDialog(val visible: Boolean) : InventoryUiEvent
    data class StartEdit(val product: Product?) : InventoryUiEvent
    data class StartAdjust(val product: Product?) : InventoryUiEvent

    data class SaveNewProduct(
        val name: String,
        val description: String,
        val unitPrice: Double,
        val unit: String,
        val barcode: String?,
        val category: String,
        val initialStock: Int,
        val minimumStock: Int,
    ) : InventoryUiEvent

    data class SaveEditedProduct(
        val id: String,
        val name: String,
        val description: String,
        val unitPrice: Double,
        val unit: String,
        val barcode: String?,
        val category: String,
        val stock: Int,
        val minimumStock: Int,
    ) : InventoryUiEvent

    data class ApplyStockAdjustment(
        val productId: String,
        val quantityDelta: Int,
        val reason: String,
        val type: StockAdjustmentType,
    ) : InventoryUiEvent
}

sealed interface InventoryUiEffect : UiEffect {
    data class ShowMessage(val message: String) : InventoryUiEffect
}

