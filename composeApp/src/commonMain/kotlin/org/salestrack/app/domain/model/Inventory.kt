package org.salestrack.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class StockAdjustmentType {
    Entry,
    PhysicalCount,
    Loss,
    Sale,
    Return,
}

@Serializable
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val unitPrice: Double,
    val unit: String,
    val barcode: String?,
    val category: String,
    val stock: Int,
    val minimumStock: Int,
    val isActive: Boolean = true,
)

data class NewProductInput(
    val name: String,
    val description: String,
    val unitPrice: Double,
    val unit: String,
    val barcode: String?,
    val category: String,
    val initialStock: Int,
    val minimumStock: Int,
)

@Serializable
data class StockMovement(
    val id: String,
    val productId: String,
    val type: StockAdjustmentType,
    val quantityDelta: Int,
    val reason: String,
    val sellerName: String? = null,
    val platform: String? = null,
    val createdAtMillis: Long,
)

data class CatalogImportError(
    val line: Int,
    val reason: String,
)

data class CatalogImportResult(
    val totalRows: Int,
    val importedRows: Int,
    val failedRows: Int,
    val errors: List<CatalogImportError>,
)

data class CatalogExportFile(
    val fileName: String,
    val mimeType: String,
    val content: String,
)

