package org.salestrack.app.domain.model

enum class ExportFormat {
    Pdf,
    Excel,
    Csv,
}

enum class ExportDestination {
    Share,
    SaveLocal,
}

data class ExportRow(
    val productName: String,
    val category: String,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double,
    val netTotal: Double,
    val sellerName: String,
)

data class ExportReportPayload(
    val title: String,
    val periodLabel: String,
    val includeSellerColumn: Boolean,
    val rows: List<ExportRow>,
    val totalAmount: Double,
)

data class ExportArtifact(
    val fileName: String,
    val mimeType: String,
    val destination: ExportDestination,
    val preview: String,
)
