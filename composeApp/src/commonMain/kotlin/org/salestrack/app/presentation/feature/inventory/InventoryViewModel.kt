package org.salestrack.app.presentation.feature.inventory

import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.NewProductInput
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockMovement
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.usecase.inventory.AddProductUseCase
import org.salestrack.app.domain.usecase.inventory.AdjustStockUseCase
import org.salestrack.app.domain.usecase.inventory.EditProductUseCase
import org.salestrack.app.domain.usecase.inventory.ExportCatalogCsvUseCase
import org.salestrack.app.domain.usecase.inventory.ExportCatalogExcelUseCase
import org.salestrack.app.domain.usecase.inventory.FilterProductsUseCase
import org.salestrack.app.domain.usecase.inventory.GetLowStockProductsUseCase
import org.salestrack.app.domain.usecase.inventory.ImportCatalogCsvUseCase

class InventoryViewModel(
    dispatcherProvider: DispatcherProvider,
    private val repository: InventoryRepository,
    private val addProductUseCase: AddProductUseCase,
    private val editProductUseCase: EditProductUseCase,
    private val filterProductsUseCase: FilterProductsUseCase,
    private val adjustStockUseCase: AdjustStockUseCase,
    private val getLowStockProductsUseCase: GetLowStockProductsUseCase,
    private val importCatalogCsvUseCase: ImportCatalogCsvUseCase,
    private val exportCatalogCsvUseCase: ExportCatalogCsvUseCase,
    private val exportCatalogExcelUseCase: ExportCatalogExcelUseCase,
) : BaseViewModel<InventoryUiState, InventoryUiEvent, InventoryUiEffect>(
    initialState = InventoryUiState(),
    dispatcherProvider = dispatcherProvider,
) {

    private var latestProducts: List<Product> = emptyList()
    private var latestMovements: List<StockMovement> = emptyList()

    init {
        observeProducts()
        observeMovements()
        refreshLowStock()
    }

    override fun onEvent(event: InventoryUiEvent) {
        when (event) {
            InventoryUiEvent.Refresh -> applyFilters()
            is InventoryUiEvent.SectionChanged -> setState { it.copy(selectedSection = event.section) }
            is InventoryUiEvent.QueryChanged -> {
                setState { it.copy(query = event.value) }
                applyFilters()
            }
            is InventoryUiEvent.CategoryChanged -> {
                setState { it.copy(selectedCategory = event.value) }
                applyFilters()
            }
            is InventoryUiEvent.SelectProduct -> {
                setState { it.copy(selectedProductId = event.productId) }
                applyFilters()
            }
            is InventoryUiEvent.CsvImportInputChanged -> setState { it.copy(csvImportInput = event.value) }
            InventoryUiEvent.ImportCatalogFromCsv -> importCatalog()
            InventoryUiEvent.ExportCatalogAsCsv -> exportCatalogAsCsv()
            InventoryUiEvent.ExportCatalogAsExcel -> exportCatalogAsExcel()
            InventoryUiEvent.ClearImportResult -> setState { it.copy(importResult = null) }
            InventoryUiEvent.ClearExportResult -> setState { it.copy(lastCsvExport = null, lastExcelExport = null) }
            is InventoryUiEvent.ToggleAddDialog -> setState { it.copy(isAddDialogVisible = event.visible) }
            is InventoryUiEvent.StartEdit -> setState { it.copy(editingProduct = event.product) }
            is InventoryUiEvent.StartAdjust -> setState { it.copy(adjustingProduct = event.product) }
            is InventoryUiEvent.SaveNewProduct -> saveNewProduct(event)
            is InventoryUiEvent.SaveEditedProduct -> saveEditedProduct(event)
            is InventoryUiEvent.ApplyStockAdjustment -> applyStockAdjustment(event)
        }
    }

    private fun observeProducts() {
        scope.launch {
            repository.observeProducts().collect { products ->
                latestProducts = products
                val categories = products.map { it.category }.distinct().sorted()
                setState { it.copy(isLoading = false, availableCategories = categories) }
                applyFilters()
            }
        }
    }

    private fun observeMovements() {
        scope.launch {
            repository.observeStockMovements().collect { movements ->
                latestMovements = movements
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val current = state.value
        val filteredProducts = filterProductsUseCase(
            products = latestProducts,
            query = current.query,
            category = current.selectedCategory,
        )

        val selectedId = current.selectedProductId
            ?.takeIf { candidate -> filteredProducts.any { it.id == candidate } }
            ?: filteredProducts.firstOrNull()?.id

        val selectedMovements = latestMovements
            .filter { movement -> selectedId != null && movement.productId == selectedId }
            .sortedByDescending { it.createdAtMillis }

        setState {
            it.copy(
                products = filteredProducts,
                selectedProductId = selectedId,
                selectedProductMovements = selectedMovements,
                errorMessage = null,
            )
        }
    }

    private fun saveNewProduct(event: InventoryUiEvent.SaveNewProduct) {
        scope.launch {
            when (
                val result = addProductUseCase(
                    NewProductInput(
                        name = event.name,
                        description = event.description,
                        unitPrice = event.unitPrice,
                        unit = event.unit,
                        barcode = event.barcode?.takeIf { it.isNotBlank() },
                        category = event.category,
                        initialStock = event.initialStock,
                        minimumStock = event.minimumStock,
                    ),
                )
            ) {
                is AppResult.Success -> {
                    setState { it.copy(isAddDialogVisible = false) }
                    refreshLowStock()
                    emitEffect(InventoryUiEffect.ShowMessage("Producto creado"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "Error creando producto") }
                }
            }
        }
    }

    private fun saveEditedProduct(event: InventoryUiEvent.SaveEditedProduct) {
        scope.launch {
            val existing = latestProducts.firstOrNull { it.id == event.id }
            if (existing == null) {
                setState { it.copy(errorMessage = "Producto no encontrado") }
                return@launch
            }

            when (
                val result = editProductUseCase(
                    existing.copy(
                        name = event.name,
                        description = event.description,
                        unitPrice = event.unitPrice,
                        unit = event.unit,
                        barcode = event.barcode?.takeIf { it.isNotBlank() },
                        category = event.category,
                        stock = event.stock,
                        minimumStock = event.minimumStock,
                    ),
                )
            ) {
                is AppResult.Success -> {
                    setState { it.copy(editingProduct = null) }
                    refreshLowStock()
                    emitEffect(InventoryUiEffect.ShowMessage("Producto actualizado"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "Error editando producto") }
                }
            }
        }
    }

    private fun applyStockAdjustment(event: InventoryUiEvent.ApplyStockAdjustment) {
        scope.launch {
            when (
                val result = adjustStockUseCase(
                    productId = event.productId,
                    quantityDelta = event.quantityDelta,
                    reason = event.reason,
                    type = event.type,
                    platform = "App",
                )
            ) {
                is AppResult.Success -> {
                    setState { it.copy(adjustingProduct = null) }
                    refreshLowStock()
                    emitEffect(InventoryUiEffect.ShowMessage("Stock actualizado"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "Error ajustando stock") }
                }
            }
        }
    }

    private fun refreshLowStock() {
        scope.launch {
            when (val result = getLowStockProductsUseCase()) {
                is AppResult.Success -> {
                    setState { it.copy(lowStockProducts = result.value) }
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "Error consultando stock bajo") }
                }
            }
        }
    }

    private fun importCatalog() {
        scope.launch {
            val csvContent = state.value.csvImportInput
            when (val result = importCatalogCsvUseCase(csvContent)) {
                is AppResult.Success -> {
                    setState {
                        it.copy(
                            importResult = result.value,
                            errorMessage = null,
                        )
                    }
                    refreshLowStock()
                    emitEffect(
                        InventoryUiEffect.ShowMessage(
                            "Importacion completada: ${result.value.importedRows}/${result.value.totalRows}",
                        ),
                    )
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "Error importando catalogo") }
                }
            }
        }
    }

    private fun exportCatalogAsCsv() {
        scope.launch {
            when (val result = exportCatalogCsvUseCase()) {
                is AppResult.Success -> {
                    setState { it.copy(lastCsvExport = result.value, errorMessage = null) }
                    emitEffect(InventoryUiEffect.ShowMessage("CSV generado: ${result.value.fileName}"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "Error exportando CSV") }
                }
            }
        }
    }

    private fun exportCatalogAsExcel() {
        scope.launch {
            when (val result = exportCatalogExcelUseCase()) {
                is AppResult.Success -> {
                    setState { it.copy(lastExcelExport = result.value, errorMessage = null) }
                    emitEffect(InventoryUiEffect.ShowMessage("Excel generado: ${result.value.fileName}"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(errorMessage = result.error.message ?: "Error exportando Excel") }
                }
            }
        }
    }
}


