package org.salestrack.app.data.export

import org.salestrack.app.domain.model.ExportReportPayload

interface ExcelExportAdapter {
    fun generate(payload: ExportReportPayload): GeneratedDocument
}

class SpreadsheetXmlExcelExportAdapter : ExcelExportAdapter {
    override fun generate(payload: ExportReportPayload): GeneratedDocument {
        val headerCells = if (payload.includeSellerColumn) {
            listOf("Producto", "Categoria", "Cantidad", "Precio", "Descuento", "Neto", "Vendedor", "Fecha", "Mes", "Semana")
        } else {
            listOf("Producto", "Categoria", "Cantidad", "Precio", "Descuento", "Neto", "Fecha", "Mes", "Semana")
        }

        val csvContent = buildString {
            // Write UTF-8 BOM so Excel/Sheets on all platforms detect UTF-8 encoding correctly
            append('\uFEFF')
            
            // Header
            append(headerCells.joinToString(",") { "\"${escapeCsv(it)}\"" })
            append("\n")

            // Summary Info
            append("\"Resumen: ${escapeCsv(payload.title)}\",\"Periodo: ${escapeCsv(payload.periodLabel)}\",\"Total: ${payload.totalAmount}\"\n\n")

            // Detail Rows
            payload.rows.forEach { row ->
                val detail = mutableListOf(
                    row.productName,
                    row.category,
                    row.quantity.toString(),
                    row.unitPrice.toString(),
                    row.discount.toString(),
                    row.netTotal.toString(),
                )
                if (payload.includeSellerColumn) {
                    detail += row.sellerName
                }
                detail += row.dateLabel
                detail += row.monthLabel
                detail += row.weekLabel
                append(detail.joinToString(",") { "\"${escapeCsv(it)}\"" })
                append("\n")
            }
        }

        return GeneratedDocument(
            bytes = csvContent.toByteArray(Charsets.UTF_8),
            mimeType = "text/csv",
            fileExtension = "csv",
            preview = "Resumen+Detalle | cols=${headerCells.joinToString(",")}",
        )
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
