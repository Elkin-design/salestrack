package org.salestrack.app.domain.usecase.reports

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ReportData
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.domain.model.ReportRange
import org.salestrack.app.domain.repository.SaleRepository

class GetCustomRangeReportUseCase(
    private val repository: SaleRepository,
) {
    suspend operator fun invoke(
        fromMillis: Long,
        toMillis: Long,
        category: String? = null,
    ): AppResult<ReportData> {
        if (fromMillis > toMillis) {
            return AppResult.Failure(IllegalArgumentException("El rango es inválido"))
        }

        return AppResult.Success(
            ReportCalculator.build(
                sales = repository.observeSales().first(),
                period = ReportPeriod.Custom,
                range = ReportRange(fromMillis = fromMillis, toMillis = toMillis),
                category = category,
            ),
        )
    }
}

