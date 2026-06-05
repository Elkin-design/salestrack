package org.salestrack.app.presentation.feature.sales

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Sale
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.repository.SaleRepository
import org.salestrack.app.domain.usecase.sales.AddSaleUseCase
import org.salestrack.app.domain.usecase.sales.DeleteSaleUseCase
import org.salestrack.app.domain.usecase.sales.FilterSalesUseCase
import org.salestrack.app.domain.usecase.sales.UpdateSaleUseCase
import org.salestrack.app.domain.usecase.category.ObserveCategoriesUseCase
import org.salestrack.app.domain.model.Category

class SalesViewModel(
    dispatcherProvider: DispatcherProvider,
    private val repository: SaleRepository,
    private val inventoryRepository: InventoryRepository,
    private val addSaleUseCase: AddSaleUseCase,
    private val updateSaleUseCase: UpdateSaleUseCase,
    private val deleteSaleUseCase: DeleteSaleUseCase,
    private val filterSalesUseCase: FilterSalesUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase? = null,
) : BaseViewModel<SalesUiState, SalesUiEvent, SalesUiEffect>(
    initialState = SalesUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    private var latestSales: List<Sale> = emptyList()
    private var latestCategories: List<Category> = emptyList()

    init {
        observeSales()
        observeInventory()
        observeCategories()
    }

    override fun onEvent(event: SalesUiEvent) {
        when (event) {
            is SalesUiEvent.QueryChanged -> {
                setState { it.copy(query = event.value) }
                applyFilters()
            }
            is SalesUiEvent.CategoryChanged -> {
                setState { it.copy(selectedCategory = event.value) }
                applyFilters()
            }
            is SalesUiEvent.ToggleAddDialog -> setState { it.copy(isAddDialogVisible = event.visible) }
            is SalesUiEvent.ShowDetail -> setState { it.copy(detailSale = event.sale) }
            is SalesUiEvent.StartEdit -> setState { it.copy(editingSale = event.sale) }
            is SalesUiEvent.SaveNewSale -> saveNewSale(event)
            is SalesUiEvent.SaveEditedSale -> saveEditedSale(event)
            is SalesUiEvent.DeleteSale -> deleteSale(event.saleId)
            SalesUiEvent.Refresh -> applyFilters()
        }
    }

    private fun observeSales() {
        scope.launch {
            repository.observeSales().collect { sales ->
                latestSales = sales
                applyFilters()
            }
        }
    }

    private fun observeCategories() {
        val useCase = observeCategoriesUseCase ?: return
        scope.launch {
            useCase().collect { categories ->
                latestCategories = categories.filter { it.isActive }
                val names = latestCategories.map { it.name }.distinct().sortedBy { it.lowercase() }
                setState { it.copy(isLoading = false, availableCategories = names) }
            }
        }
    }

    private fun observeInventory() {
        scope.launch {
            inventoryRepository.observeProducts().collect { products ->
                setState { it.copy(inventoryProducts = products) }
            }
        }
    }

    private fun applyFilters() {
        val current = state.value
        val filtered = filterSalesUseCase(
            sales = latestSales,
            query = current.query,
            category = current.selectedCategory,
        )
        setState { it.copy(sales = filtered, errorMessage = null) }
    }

    private fun saveNewSale(event: SalesUiEvent.SaveNewSale) {
        scope.launch {
            setState { it.copy(isSaving = true, errorMessage = null) }
            delay(1200) // Demora de animación premium para simular guardado en base de datos
            val result = addSaleUseCase(
                NewSaleInput(
                    productName = event.productName,
                    category = event.category,
                    quantity = event.quantity,
                    unitPrice = event.unitPrice,
                    discount = event.discount,
                    sellerName = event.seller,
                    productId = event.productId,
                ),
            )
            when (result) {
                is AppResult.Success -> {
                    setState { it.copy(isSaving = false, isAddDialogVisible = false) }
                    emitEffect(SalesUiEffect.ShowMessage("Venta creada"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(isSaving = false, errorMessage = result.error.message ?: "Error al crear venta") }
                }
            }
        }
    }

    private fun saveEditedSale(event: SalesUiEvent.SaveEditedSale) {
        scope.launch {
            setState { it.copy(isSaving = true, errorMessage = null) }
            delay(1200) // Demora de animación premium para simular guardado en base de datos
            val result = updateSaleUseCase(
                Sale(
                    id = event.id,
                    productName = event.productName,
                    category = event.category,
                    quantity = event.quantity,
                    unitPrice = event.unitPrice,
                    discount = event.discount,
                    sellerName = event.seller,
                    productId = event.productId,
                    createdAtMillis = latestSales.firstOrNull { it.id == event.id }?.createdAtMillis ?: 0L,
                ),
            )
            when (result) {
                is AppResult.Success -> {
                    setState { it.copy(isSaving = false, editingSale = null) }
                    emitEffect(SalesUiEffect.ShowMessage("Venta actualizada"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(isSaving = false, errorMessage = result.error.message ?: "Error al editar venta") }
                }
            }
        }
    }

    private fun deleteSale(saleId: String) {
        scope.launch {
            when (val result = deleteSaleUseCase(saleId)) {
                is AppResult.Success<*> -> emitEffect(SalesUiEffect.ShowMessage("Venta eliminada"))
                is AppResult.Failure -> setState {
                    it.copy(errorMessage = result.error.message ?: "Error al eliminar venta")
                }
            }
        }
    }
}

