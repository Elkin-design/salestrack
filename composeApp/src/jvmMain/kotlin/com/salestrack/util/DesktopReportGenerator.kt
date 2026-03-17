package com.salestrack.util

import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import com.salestrack.domain.model.ReportData
import com.salestrack.domain.model.Sale
import com.salestrack.domain.util.ReportGenerator
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

class DesktopReportGenerator : ReportGenerator {

    override fun generatePdf(title: String, data: ReportData, sales: List<Sale>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val document = Document()
        PdfWriter.getInstance(document, outputStream)
        
        document.open()
        document.add(Paragraph(title))
        document.add(Paragraph("Total Sales: $${data.totalSales}"))
        document.add(Paragraph("Sale Count: ${data.count}"))
        document.add(Paragraph("Best Seller: ${data.bestSellingProduct ?: "N/A"}"))
        document.add(Paragraph("\nSales Detail:"))
        
        sales.take(50).forEach { sale ->
            document.add(Paragraph("${sale.productName} - Qty: ${sale.quantity} - Total: $${sale.totalAmount}"))
        }
        
        document.close()
        return outputStream.toByteArray()
    }

    override fun generateExcel(title: String, sales: List<Sale>): ByteArray {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Sales Report")
        
        // Header
        val headerRow = sheet.createRow(0)
        listOf("ID", "Product", "Quantity", "Price", "Total", "Date").forEachIndexed { index, name ->
            headerRow.createCell(index).setCellValue(name)
        }
        
        // Data
        sales.forEachIndexed { index, sale ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(sale.id)
            row.createCell(1).setCellValue(sale.productName)
            row.createCell(2).setCellValue(sale.quantity.toDouble())
            row.createCell(3).setCellValue(sale.unitPrice)
            row.createCell(4).setCellValue(sale.totalAmount)
            row.createCell(5).setCellValue(sale.timestamp.toString())
        }
        
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()
        
        return outputStream.toByteArray()
    }
}
