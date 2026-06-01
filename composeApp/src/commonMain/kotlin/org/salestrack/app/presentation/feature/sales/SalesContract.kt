package org.salestrack.app.presentation.feature.sales

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.Sale

data class SalesUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategory: String? = null,
    val availableCategories: List<String> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val inventoryProducts: List<Product> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val editingSale: Sale? = null,
    val detailSale: Sale? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
) : UiState

sealed interface SalesUiEvent : UiEvent {
    data object Refresh : SalesUiEvent
    data class QueryChanged(val value: String) : SalesUiEvent
    data class CategoryChanged(val value: String?) : SalesUiEvent
    data class ToggleAddDialog(val visible: Boolean) : SalesUiEvent
    data class ShowDetail(val sale: Sale?) : SalesUiEvent
    data class StartEdit(val sale: Sale?) : SalesUiEvent
    data class SaveNewSale(
        val productName: String,
        val category: String,
        val quantity: Int,
        val unitPrice: Double,
        val discount: Double,
        val seller: String,
        val productId: String? = null,
    ) : SalesUiEvent
    data class SaveEditedSale(
        val id: String,
        val productName: String,
        val category: String,
        val quantity: Int,
        val unitPrice: Double,
        val discount: Double,
        val seller: String,
        val productId: String? = null,
    ) : SalesUiEvent
    data class DeleteSale(val saleId: String) : SalesUiEvent
}

sealed interface SalesUiEffect : UiEffect {
    data class ShowMessage(val message: String) : SalesUiEffect
}

