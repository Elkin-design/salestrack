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

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ExportExcelUseCase(
    private val saleRepository: SaleRepository,
    private val exportRepository: ExportRepository,
) {
    suspend operator fun invoke(
        destination: ExportDestination,
        includeSellerColumn: Boolean = false,
    ): AppResult<ExportArtifact> {
        val sales = saleRepository.observeSales().first().filter { !it.isDeleted }
        val timeZone = TimeZone.currentSystemDefault()
        val rows = sales.map { sale ->
            val localDateTime = Instant.fromEpochMilliseconds(sale.createdAtMillis).toLocalDateTime(timeZone)
            val dateLabel = "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${localDateTime.monthNumber.toString().padStart(2, '0')}/${localDateTime.year}"
            val monthLabel = when (localDateTime.monthNumber) {
                1 -> "Enero"
                2 -> "Febrero"
                3 -> "Marzo"
                4 -> "Abril"
                5 -> "Mayo"
                6 -> "Junio"
                7 -> "Julio"
                8 -> "Agosto"
                9 -> "Septiembre"
                10 -> "Octubre"
                11 -> "Noviembre"
                12 -> "Diciembre"
                else -> ""
            }
            val weekOfMonth = ((localDateTime.dayOfMonth - 1) / 7) + 1
            val weekLabel = "Semana $weekOfMonth"

            ExportRow(
                productName = sale.productName,
                category = sale.category,
                quantity = sale.quantity,
                unitPrice = sale.unitPrice,
                discount = sale.discount,
                netTotal = sale.netTotal,
                sellerName = sale.sellerName,
                dateLabel = dateLabel,
                monthLabel = monthLabel,
                weekLabel = weekLabel,
            )
        }
        val payload = ExportReportPayload(
            title = "Reporte Excel de ventas",
            periodLabel = "Historico",
            includeSellerColumn = includeSellerColumn,
            rows = rows,
            totalAmount = rows.sumOf { it.netTotal },
        )
        return exportRepository.exportReport(payload, ExportFormat.Excel, destination)
    }
}
