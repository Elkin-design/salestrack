package org.salestrack.app.data.export

import org.salestrack.app.domain.model.ExportReportPayload

data class GeneratedDocument(
    val bytes: ByteArray,
    val mimeType: String,
    val fileExtension: String,
    val preview: String,
)

interface PdfExportAdapter {
    fun generate(payload: ExportReportPayload): GeneratedDocument
}

class BasicPdfExportAdapter : PdfExportAdapter {
    override fun generate(payload: ExportReportPayload): GeneratedDocument {
        val lines = buildList {
            add(payload.title)
            add("Periodo: ${payload.periodLabel}")
            add("Total: ${payload.totalAmount}")
            add("")
            add(
                if (payload.includeSellerColumn) {
                    "Producto | Categoria | Cantidad | Neto | Vendedor | Fecha | Mes | Sem."
                } else {
                    "Producto | Categoria | Cantidad | Neto | Fecha | Mes | Sem."
                },
            )
            payload.rows.take(40).forEach { row ->
                add(
                    if (payload.includeSellerColumn) {
                        "${row.productName} | ${row.category} | ${row.quantity} | ${row.netTotal} | ${row.sellerName} | ${row.dateLabel} | ${row.monthLabel} | ${row.weekLabel}"
                    } else {
                        "${row.productName} | ${row.category} | ${row.quantity} | ${row.netTotal} | ${row.dateLabel} | ${row.monthLabel} | ${row.weekLabel}"
                    },
                )
            }
        }

        return GeneratedDocument(
            bytes = createPdf(lines),
            mimeType = "application/pdf",
            fileExtension = "pdf",
            preview = lines.take(5).joinToString(" | "),
        )
    }

    private fun createPdf(lines: List<String>): ByteArray {
        val content = buildString {
            append("BT\n")
            append("/F1 11 Tf\n")
            var y = 800
            lines.forEach { line ->
                val escaped = line
                    .replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                append("1 0 0 1 40 $y Tm\n")
                append("($escaped) Tj\n")
                y -= 14
            }
            append("ET\n")
        }

        val objects = listOf(
            "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n",
            "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n",
            "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n",
            "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n",
            "5 0 obj\n<< /Length ${content.toByteArray(Charsets.UTF_8).size} >>\nstream\n$content\nendstream\nendobj\n",
        )

        val header = "%PDF-1.4\n"
        val body = StringBuilder(header)
        val offsets = mutableListOf(0)

        objects.forEach { obj ->
            offsets += body.toString().toByteArray(Charsets.UTF_8).size
            body.append(obj)
        }

        val xrefStart = body.toString().toByteArray(Charsets.UTF_8).size
        body.append("xref\n0 ${offsets.size}\n")
        body.append("0000000000 65535 f \n")
        offsets.drop(1).forEach { offset ->
            body.append(offset.toString().padStart(10, '0')).append(" 00000 n \n")
        }

        body.append("trailer\n<< /Size ${offsets.size} /Root 1 0 R >>\n")
        body.append("startxref\n$xrefStart\n%%EOF")

        return body.toString().toByteArray(Charsets.UTF_8)
    }
}
