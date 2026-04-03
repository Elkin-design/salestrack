package org.salestrack.app.domain.usecase.export

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ExportArtifact
import org.salestrack.app.domain.model.ExportDestination
import org.salestrack.app.domain.model.ExportFormat
import org.salestrack.app.domain.model.ExportReportPayload
import org.salestrack.app.domain.model.ExportRow
import org.salestrack.app.domain.repository.ExportRepository
import org.salestrack.app.domain.repository.SaleRepository

class ExportCsvUseCase(
    private val saleRepository: SaleRepository,
    private val exportRepository: ExportRepository,
) {
    suspend operator fun invoke(destination: ExportDestination): AppResult<ExportArtifact> {
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
            title = "Reporte de ventas",
            periodLabel = "Historico",
            includeSellerColumn = true,
            rows = rows,
            totalAmount = rows.sumOf { it.netTotal },
        )
        return exportRepository.exportReport(payload, ExportFormat.Csv, destination)
    }
}
