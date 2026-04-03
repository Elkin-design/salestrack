package org.salestrack.app.presentation.feature.category

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Category
import org.salestrack.app.domain.usecase.category.CreateCategoryUseCase
import org.salestrack.app.domain.usecase.category.DeleteCategoryUseCase
import org.salestrack.app.domain.usecase.category.ObserveCategoriesUseCase
import org.salestrack.app.domain.usecase.category.UpdateCategoryUseCase

class CategoryManagementViewModel(
    dispatcherProvider: DispatcherProvider,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
) : BaseViewModel<CategoryManagementUiState, CategoryManagementUiEvent, CategoryManagementUiEffect>(
    initialState = CategoryManagementUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    init {
        observeCategories()
    }

    override fun onEvent(event: CategoryManagementUiEvent) {
        when (event) {
            is CategoryManagementUiEvent.NewNameChanged -> setState { it.copy(newCategoryName = event.value) }
            is CategoryManagementUiEvent.NewColorChanged -> setState { it.copy(newCategoryColorHex = event.value) }
            CategoryManagementUiEvent.SaveNewCategory -> saveNewCategory()
            is CategoryManagementUiEvent.StartEdit -> beginEdit(event.category)
            is CategoryManagementUiEvent.EditNameChanged -> setState { it.copy(editingName = event.value) }
            is CategoryManagementUiEvent.EditColorChanged -> setState { it.copy(editingColorHex = event.value) }
            CategoryManagementUiEvent.SaveEditedCategory -> saveEditedCategory()
            is CategoryManagementUiEvent.DeleteCategory -> deleteCategory(event.categoryId)
        }
    }

    private fun observeCategories() {
        scope.launch {
            observeCategoriesUseCase().collect { categories ->
                setState {
                    it.copy(
                        isLoading = false,
                        categories = categories.filter { category -> category.isActive },
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun saveNewCategory() {
        scope.launch {
            val current = state.value
            when (val result = createCategoryUseCase(current.newCategoryName, current.newCategoryColorHex)) {
                is AppResult.Success -> {
                    setState { it.copy(newCategoryName = "", newCategoryColorHex = "#1E88E5", errorMessage = null) }
                    emitEffect(CategoryManagementUiEffect.ShowMessage("Categoria creada"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "No se pudo crear categoria") }
                }
            }
        }
    }

    private fun beginEdit(category: Category?) {
        setState {
            it.copy(
                editingCategory = category,
                editingName = category?.name ?: "",
                editingColorHex = category?.colorHex ?: "#1E88E5",
            )
        }
    }

    private fun saveEditedCategory() {
        scope.launch {
            val current = state.value
            val category = current.editingCategory ?: return@launch

            when (
                val result = updateCategoryUseCase(
                    category.copy(
                        name = current.editingName,
                        colorHex = current.editingColorHex,
                    ),
                )
            ) {
                is AppResult.Success -> {
                    setState {
                        it.copy(
                            editingCategory = null,
                            editingName = "",
                            editingColorHex = "#1E88E5",
                            errorMessage = null,
                        )
                    }
                    emitEffect(CategoryManagementUiEffect.ShowMessage("Categoria actualizada"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "No se pudo actualizar categoria") }
                }
            }
        }
    }

    private fun deleteCategory(categoryId: String) {
        scope.launch {
            when (val result = deleteCategoryUseCase(categoryId)) {
                is AppResult.Success -> emitEffect(CategoryManagementUiEffect.ShowMessage("Categoria eliminada"))
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "No se pudo eliminar categoria") }
                }
            }
        }
    }
}
