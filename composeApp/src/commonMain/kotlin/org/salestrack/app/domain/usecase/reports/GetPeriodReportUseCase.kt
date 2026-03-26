package org.salestrack.app.domain.usecase.reports

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.ReportData
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.domain.model.ReportRange
import org.salestrack.app.domain.repository.SaleRepository

class GetPeriodReportUseCase(
    private val repository: SaleRepository,
) {
    suspend operator fun invoke(
        period: ReportPeriod,
        nowMillis: Long,
        fromMillis: Long,
        toMillis: Long,
        category: String?,
    ): AppResult<ReportData> {
        val range = when (period) {
            ReportPeriod.Daily -> ReportRange(nowMillis - HALF_DAY, nowMillis + HALF_DAY)
            ReportPeriod.Weekly -> ReportRange(nowMillis - HALF_WEEK, nowMillis + HALF_WEEK)
            ReportPeriod.Monthly -> ReportRange(nowMillis - HALF_MONTH, nowMillis + HALF_MONTH)
            ReportPeriod.Annual -> ReportRange(nowMillis - HALF_YEAR, nowMillis + HALF_YEAR)
            ReportPeriod.Custom -> {
                if (fromMillis > toMillis) {
                    return AppResult.Failure(IllegalArgumentException("El rango es inválido"))
                }
                ReportRange(fromMillis = fromMillis, toMillis = toMillis)
            }
        }

        return AppResult.Success(
            ReportCalculator.build(
                sales = repository.observeSales().first(),
                period = period,
                range = range,
                category = category,
            ),
        )
    }

    private companion object {
        const val HALF_DAY = 12L * 60L * 60L * 1000L
        const val HALF_WEEK = 3L * 24L * 60L * 60L * 1000L
        const val HALF_MONTH = 15L * 24L * 60L * 60L * 1000L
        const val HALF_YEAR = 182L * 24L * 60L * 60L * 1000L
    }
}

