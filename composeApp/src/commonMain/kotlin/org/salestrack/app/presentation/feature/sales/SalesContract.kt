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
    val detailSale: Sale? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
) : UiState

sealed interface SalesUiEvent : UiEvent {
    data object Refresh : SalesUiEvent
    data class QueryChanged(val value: String) : SalesUiEvent
    data class CategoryChanged(val value: String?) : SalesUiEvent
    data class ShowDetail(val sale: Sale?) : SalesUiEvent
    data class DeleteSale(val saleId: String) : SalesUiEvent
}

sealed interface SalesUiEffect : UiEffect {
    data class ShowMessage(val message: String) : SalesUiEffect
}

