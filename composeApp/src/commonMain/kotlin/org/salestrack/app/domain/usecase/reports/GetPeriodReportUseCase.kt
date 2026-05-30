package org.salestrack.app.domain.usecase.reports

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.LocalDateTime
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
        offset: Int = 0,
    ): AppResult<ReportData> {
        val timeZone = TimeZone.currentSystemDefault()
        val nowDateTime = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(timeZone)

        val todayStartMillis = LocalDateTime(
            nowDateTime.year,
            nowDateTime.monthNumber,
            nowDateTime.dayOfMonth,
            0, 0, 0, 0
        ).toInstant(timeZone).toEpochMilliseconds()

        val range = when (period) {
            ReportPeriod.Daily -> {
                val targetMillis = todayStartMillis + (offset * MILLIS_PER_DAY)
                ReportRange(targetMillis, targetMillis + MILLIS_PER_DAY - 1)
            }
            ReportPeriod.Weekly -> {
                val targetEndDayMillis = todayStartMillis + (offset * 7L * MILLIS_PER_DAY)
                val targetStartDayMillis = targetEndDayMillis - (6L * MILLIS_PER_DAY)
                ReportRange(targetStartDayMillis, targetEndDayMillis + MILLIS_PER_DAY - 1)
            }
            ReportPeriod.Monthly -> {
                var y = nowDateTime.year
                var m = nowDateTime.monthNumber + offset
                while (m < 1) { m += 12; y -= 1 }
                while (m > 12) { m -= 12; y += 1 }
                
                val startOfMonthMillis = LocalDateTime(y, m, 1, 0, 0, 0, 0).toInstant(timeZone).toEpochMilliseconds()
                
                var nextM = m + 1
                var nextY = y
                if (nextM > 12) { nextM = 1; nextY += 1 }
                val startOfNextMonthMillis = LocalDateTime(nextY, nextM, 1, 0, 0, 0, 0).toInstant(timeZone).toEpochMilliseconds()
                
                ReportRange(startOfMonthMillis, startOfNextMonthMillis - 1)
            }
            ReportPeriod.Annual -> {
                val y = nowDateTime.year + offset
                val startOfYearMillis = LocalDateTime(y, 1, 1, 0, 0, 0, 0).toInstant(timeZone).toEpochMilliseconds()
                val startOfNextYearMillis = LocalDateTime(y + 1, 1, 1, 0, 0, 0, 0).toInstant(timeZone).toEpochMilliseconds()
                ReportRange(startOfYearMillis, startOfNextYearMillis - 1)
            }
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
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}

