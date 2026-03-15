package com.salestrack.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.salestrack.domain.model.ReportData
import com.salestrack.domain.model.Sale
import com.salestrack.domain.util.ReportGenerator
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream

class AndroidReportGenerator : ReportGenerator {

    override fun generatePdf(title: String, data: ReportData, sales: List<Sale>): ByteArray {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        var y = 50f
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText(title, 50f, y, paint)

        y += 40f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Total Sales: $${data.totalSales}", 50f, y, paint)
        
        y += 20f
        canvas.drawText("Sale Count: ${data.count}", 50f, y, paint)

        y += 20f
        canvas.drawText("Best Seller: ${data.bestSellingProduct ?: "N/A"}", 50f, y, paint)

        y += 40f
        paint.isFakeBoldText = true
        canvas.drawText("Sales Detail:", 50f, y, paint)
        paint.isFakeBoldText = false

        y += 30f
        sales.take(20).forEach { sale ->
            canvas.drawText("${sale.productName} - Qty: ${sale.quantity} - Total: $${sale.totalAmount}", 50f, y, paint)
            y += 20f
            if (y > 800) return@forEach // Simple limit for one page
        }

        pdfDocument.finishPage(page)
        
        val outputStream = ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        
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
