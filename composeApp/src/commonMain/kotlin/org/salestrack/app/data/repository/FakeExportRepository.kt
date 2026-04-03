package org.salestrack.app.data.repository

import org.salestrack.app.data.export.ExcelExportAdapter
import org.salestrack.app.data.export.GeneratedDocument
import org.salestrack.app.data.export.PdfExportAdapter
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ExportArtifact
import org.salestrack.app.domain.model.ExportDestination
import org.salestrack.app.domain.model.ExportFormat
import org.salestrack.app.domain.model.ExportReportPayload
import org.salestrack.app.domain.repository.ExportRepository

class RealExportRepository(
    private val pdfAdapter: PdfExportAdapter,
    private val excelAdapter: ExcelExportAdapter,
) : ExportRepository {
    override suspend fun exportReport(
        payload: ExportReportPayload,
        format: ExportFormat,
        destination: ExportDestination,
    ): AppResult<ExportArtifact> {
        val generated = when (format) {
            ExportFormat.Pdf -> pdfAdapter.generate(payload)
            ExportFormat.Excel -> excelAdapter.generate(payload)
            ExportFormat.Csv -> generateCsv(payload)
        }

        return AppResult.Success(
            ExportArtifact(
                fileName = "salestrack_report.${generated.fileExtension}",
                mimeType = generated.mimeType,
                destination = destination,
                preview = generated.preview,
            ),
        )
    }

    private fun generateCsv(payload: ExportReportPayload): GeneratedDocument {
        val header = if (payload.includeSellerColumn) {
            "Producto,Categoria,Cantidad,Precio,Descuento,Neto,Vendedor"
        } else {
            "Producto,Categoria,Cantidad,Precio,Descuento,Neto"
        }

        val lines = payload.rows.map { row ->
            if (payload.includeSellerColumn) {
                "${escapeCsv(row.productName)},${escapeCsv(row.category)},${row.quantity},${row.unitPrice},${row.discount},${row.netTotal},${escapeCsv(row.sellerName)}"
            } else {
                "${escapeCsv(row.productName)},${escapeCsv(row.category)},${row.quantity},${row.unitPrice},${row.discount},${row.netTotal}"
            }
        }

        val content = (listOf(header) + lines).joinToString("\n")
        return GeneratedDocument(
            bytes = content.toByteArray(Charsets.UTF_8),
            mimeType = "text/csv",
            fileExtension = "csv",
            preview = "CSV | columnas=${header}",
        )
    }

    private fun escapeCsv(value: String): String {
        val sanitized = value.replace("\"", "\"\"")
        return "\"$sanitized\""
    }
}
