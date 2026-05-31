package org.salestrack.app.presentation.feature.category

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.Category
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.repository.InventoryRepository
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
    private val inventoryRepository: InventoryRepository? = null,
) : BaseViewModel<CategoryManagementUiState, CategoryManagementUiEvent, CategoryManagementUiEffect>(
    initialState = CategoryManagementUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    private var latestConfiguredCategories: List<Category> = emptyList()
    private var latestProducts: List<Product> = emptyList()

    init {
        observeCategories()
        observeProducts()
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
                latestConfiguredCategories = categories.filter { it.isActive }
                updateCategoriesState()
            }
        }
    }

    private fun observeProducts() {
        val repo = inventoryRepository ?: return
        scope.launch {
            repo.observeProducts().collect { products ->
                latestProducts = products
                updateCategoriesState()
            }
        }
    }

    private fun updateCategoriesState() {
        val configured = latestConfiguredCategories
        
        val productCategories = latestProducts
            .map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            
        val tempCategories = productCategories.mapNotNull { name ->
            val alreadyExists = configured.any { it.name.equals(name, ignoreCase = true) }
            if (!alreadyExists) {
                Category(
                    id = "TEMP-${name.lowercase()}",
                    name = name,
                    colorHex = "#9E9E9E", 
                    updatedAtMillis = 0L
                )
            } else null
        }
        
        val merged = (configured + tempCategories).sortedBy { it.name.lowercase() }
        
        setState {
            it.copy(
                isLoading = false,
                categories = merged,
                errorMessage = null
            )
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

            val categoryToSave = if (category.id.startsWith("TEMP-")) {
                Category(
                    id = "C-${System.currentTimeMillis()}-1",
                    name = current.editingName,
                    colorHex = current.editingColorHex,
                    updatedAtMillis = System.currentTimeMillis()
                )
            } else {
                category.copy(
                    name = current.editingName,
                    colorHex = current.editingColorHex,
                )
            }

            when (val result = updateCategoryUseCase(categoryToSave)) {
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
