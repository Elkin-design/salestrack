package org.salestrack.app.presentation.feature.sales

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

class SalesViewModel(
    dispatcherProvider: DispatcherProvider,
    private val repository: SaleRepository,
    private val inventoryRepository: InventoryRepository,
    private val addSaleUseCase: AddSaleUseCase,
    private val updateSaleUseCase: UpdateSaleUseCase,
    private val deleteSaleUseCase: DeleteSaleUseCase,
    private val filterSalesUseCase: FilterSalesUseCase,
) : BaseViewModel<SalesUiState, SalesUiEvent, SalesUiEffect>(
    initialState = SalesUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    private var latestSales: List<Sale> = emptyList()

    init {
        observeSales()
        observeInventory()
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
                val categories = sales.map { it.category }.distinct().sorted()
                setState { it.copy(isLoading = false, availableCategories = categories) }
                applyFilters()
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
                    setState { it.copy(isAddDialogVisible = false) }
                    emitEffect(SalesUiEffect.ShowMessage("Venta creada"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "Error al crear venta") }
                }
            }
        }
    }

    private fun saveEditedSale(event: SalesUiEvent.SaveEditedSale) {
        scope.launch {
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
                    setState { it.copy(editingSale = null) }
                    emitEffect(SalesUiEffect.ShowMessage("Venta actualizada"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "Error al editar venta") }
                }
            }
        }
    }

    private fun deleteSale(saleId: String) {
        scope.launch {
            when (val result = deleteSaleUseCase(saleId)) {
                is AppResult.Success -> emitEffect(SalesUiEffect.ShowMessage("Venta eliminada"))
                is AppResult.Failure -> setState {
                    it.copy(errorMessage = result.error.message ?: "Error al eliminar venta")
                }
            }
        }
    }
}

