package org.salestrack.app.data.export

import org.salestrack.app.domain.model.ExportReportPayload

interface ExcelExportAdapter {
    fun generate(payload: ExportReportPayload): GeneratedDocument
}

class SpreadsheetXmlExcelExportAdapter : ExcelExportAdapter {
    override fun generate(payload: ExportReportPayload): GeneratedDocument {
        val headerCells = if (payload.includeSellerColumn) {
            listOf("Producto", "Categoria", "Cantidad", "Precio", "Descuento", "Neto", "Vendedor")
        } else {
            listOf("Producto", "Categoria", "Cantidad", "Precio", "Descuento", "Neto")
        }

        val workbookXml = buildString {
            append("<?xml version=\"1.0\"?>")
            append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" ")
            append("xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">")

            append("<Worksheet ss:Name=\"Resumen\"><Table>")
            appendRow(listOf("Titulo", payload.title))
            appendRow(listOf("Periodo", payload.periodLabel))
            appendRow(listOf("Total", payload.totalAmount.toString()))
            append("</Table></Worksheet>")

            append("<Worksheet ss:Name=\"Detalle\"><Table>")
            appendRow(headerCells)
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
                appendRow(detail)
            }
            append("</Table></Worksheet>")

            append("</Workbook>")
        }

        return GeneratedDocument(
            bytes = workbookXml.toByteArray(Charsets.UTF_8),
            mimeType = "application/vnd.ms-excel",
            fileExtension = "xls",
            preview = "Resumen+Detalle | cols=${headerCells.joinToString(",")}",
        )
    }

    private fun StringBuilder.appendRow(values: List<String>) {
        append("<Row>")
        values.forEach { value ->
            append("<Cell><Data ss:Type=\"String\">")
            append(escapeXml(value))
            append("</Data></Cell>")
        }
        append("</Row>")
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
