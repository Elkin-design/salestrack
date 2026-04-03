package org.salestrack.app.presentation.feature.category

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.Category

data class CategoryManagementUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val newCategoryName: String = "",
    val newCategoryColorHex: String = "#1E88E5",
    val editingCategory: Category? = null,
    val editingName: String = "",
    val editingColorHex: String = "#1E88E5",
    val errorMessage: String? = null,
) : UiState

sealed interface CategoryManagementUiEvent : UiEvent {
    data class NewNameChanged(val value: String) : CategoryManagementUiEvent
    data class NewColorChanged(val value: String) : CategoryManagementUiEvent
    data object SaveNewCategory : CategoryManagementUiEvent
    data class StartEdit(val category: Category?) : CategoryManagementUiEvent
    data class EditNameChanged(val value: String) : CategoryManagementUiEvent
    data class EditColorChanged(val value: String) : CategoryManagementUiEvent
    data object SaveEditedCategory : CategoryManagementUiEvent
    data class DeleteCategory(val categoryId: String) : CategoryManagementUiEvent
}

sealed interface CategoryManagementUiEffect : UiEffect {
    data class ShowMessage(val message: String) : CategoryManagementUiEffect
}
