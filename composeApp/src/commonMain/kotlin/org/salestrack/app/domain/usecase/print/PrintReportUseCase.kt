package org.salestrack.app.domain.usecase.print

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ExportReportPayload
import org.salestrack.app.domain.model.ExportRow
import org.salestrack.app.domain.repository.PrintRepository
import org.salestrack.app.domain.repository.SaleRepository

class PrintReportUseCase(
    private val saleRepository: SaleRepository,
    private val printRepository: PrintRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val sales = saleRepository.observeSales().first().filter { !it.isDeleted }
        val rows = sales.map { sale ->
            ExportRow(
                productName = sale.productName,
                category = sale.category,
                quantity = sale.quantity,
                unitPrice = sale.unitPrice,
                discount = sale.discount,
                netTotal = sale.netTotal,
                sellerName = sale.sellerName,
            )
        }
        val payload = ExportReportPayload(
            title = "Reporte para impresion",
            periodLabel = "Historico",
            includeSellerColumn = true,
            rows = rows,
            totalAmount = rows.sumOf { it.netTotal },
        )
        return printRepository.printReport(payload)
    }
}
