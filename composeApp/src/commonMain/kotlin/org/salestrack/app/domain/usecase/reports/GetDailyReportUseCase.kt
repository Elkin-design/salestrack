package org.salestrack.app.domain.usecase.reports

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ReportData
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.domain.model.ReportRange
import org.salestrack.app.domain.repository.SaleRepository

class GetDailyReportUseCase(
    private val repository: SaleRepository,
) {
    suspend operator fun invoke(dayAnchorMillis: Long, category: String? = null): AppResult<ReportData> {
        val range = ReportRange(
            fromMillis = dayAnchorMillis - HALF_DAY,
            toMillis = dayAnchorMillis + HALF_DAY,
        )
        return AppResult.Success(
            ReportCalculator.build(
                sales = repository.observeSales().first(),
                period = ReportPeriod.Daily,
                range = range,
                category = category,
            ),
        )
    }

    private companion object {
        const val HALF_DAY = 12L * 60L * 60L * 1000L
    }
}

