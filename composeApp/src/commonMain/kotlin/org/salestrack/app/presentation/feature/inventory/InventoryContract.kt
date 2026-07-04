package org.salestrack.app.presentation.feature.inventory

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.CatalogExportFile
import org.salestrack.app.domain.model.CatalogImportResult
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement

enum class InventorySection {
    Catalog,
    AddProduct,
    EditProduct,
    StockAdjustment,
    MovementHistory,
    ImportExport,
}

data class InventoryUiState(
    val isLoading: Boolean = true,
    val selectedSection: InventorySection = InventorySection.Catalog,
    val query: String = "",
    val selectedCategory: String? = null,
    val availableCategories: List<String> = emptyList(),
    val products: List<Product> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val selectedProductId: String? = null,
    val selectedProductMovements: List<StockMovement> = emptyList(),
    val csvImportInput: String = "",
    val importResult: CatalogImportResult? = null,
    val lastCsvExport: CatalogExportFile? = null,
    val lastExcelExport: CatalogExportFile? = null,
    val isAddDialogVisible: Boolean = false,
    val editingProduct: Product? = null,
    val adjustingProduct: Product? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface InventoryUiEvent : UiEvent {
    data object Refresh : InventoryUiEvent
    data class SectionChanged(val section: InventorySection) : InventoryUiEvent
    data class QueryChanged(val value: String) : InventoryUiEvent
    data class CategoryChanged(val value: String?) : InventoryUiEvent
    data class SelectProduct(val productId: String?) : InventoryUiEvent
    data class CsvImportInputChanged(val value: String) : InventoryUiEvent
    data object ImportCatalogFromCsv : InventoryUiEvent
    data object ExportCatalogAsCsv : InventoryUiEvent
    data object ExportCatalogAsExcel : InventoryUiEvent
    data object ClearImportResult : InventoryUiEvent
    data object ClearExportResult : InventoryUiEvent
    data class ToggleAddDialog(val visible: Boolean) : InventoryUiEvent
    data class StartEdit(val product: Product?) : InventoryUiEvent
    data class StartAdjust(val product: Product?) : InventoryUiEvent
    data class DeleteProduct(val productId: String) : InventoryUiEvent

    data class SaveNewProduct(
        val name: String,
        val description: String,
        val unitPrice: Double,
        val unit: String,
        val barcode: String?,
        val category: String,
        val initialStock: Int,
        val minimumStock: Int,
        val discount: Double?,
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
        val discount: Double?,
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

