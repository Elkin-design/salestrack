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
    private val fileSaver: org.salestrack.app.core.utils.FileSaver = org.salestrack.app.core.utils.platformFileSaver,
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

        val savedPath = fileSaver.saveFile(
            fileName = "salestrack_report_${System.currentTimeMillis()}.${generated.fileExtension}",
            bytes = generated.bytes
        )

        if (savedPath == null) {
            return AppResult.Failure(Throwable("No se pudo guardar el archivo localmente"))
        }

        return AppResult.Success(
            ExportArtifact(
                fileName = "salestrack_report.${generated.fileExtension}",
                mimeType = generated.mimeType,
                destination = destination,
                preview = generated.preview,
                savedPath = savedPath,
            ),
        )
    }

    private fun generateCsv(payload: ExportReportPayload): GeneratedDocument {
        val header = if (payload.includeSellerColumn) {
            "Producto,Categoria,Cantidad,Precio,Descuento,Neto,Vendedor,Fecha,Mes,Semana"
        } else {
            "Producto,Categoria,Cantidad,Precio,Descuento,Neto,Fecha,Mes,Semana"
        }

        val lines = payload.rows.map { row ->
            if (payload.includeSellerColumn) {
                "${escapeCsv(row.productName)},${escapeCsv(row.category)},${row.quantity},${row.unitPrice},${row.discount},${row.netTotal},${escapeCsv(row.sellerName)},${escapeCsv(row.dateLabel)},${escapeCsv(row.monthLabel)},${escapeCsv(row.weekLabel)}"
            } else {
                "${escapeCsv(row.productName)},${escapeCsv(row.category)},${row.quantity},${row.unitPrice},${row.discount},${row.netTotal},${escapeCsv(row.dateLabel)},${escapeCsv(row.monthLabel)},${escapeCsv(row.weekLabel)}"
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
