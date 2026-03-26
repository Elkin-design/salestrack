package org.salestrack.app.domain.usecase.reports

import org.salestrack.app.domain.model.CategoryAmount
import org.salestrack.app.domain.model.ReportData
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.domain.model.ReportPoint
import org.salestrack.app.domain.model.ReportRange
import org.salestrack.app.domain.model.ReportSummary
import org.salestrack.app.domain.model.Sale

internal object ReportCalculator {

    fun build(
        sales: List<Sale>,
        period: ReportPeriod,
        range: ReportRange,
        category: String?,
    ): ReportData {
        val filtered = sales
            .asSequence()
            .filter { !it.isDeleted }
            .filter { it.createdAtMillis in range.fromMillis..range.toMillis }
            .filter { category.isNullOrBlank() || it.category == category }
            .sortedByDescending { it.createdAtMillis }
            .toList()

        val summary = buildSummary(filtered)
        val points = buildPoints(filtered, period)

        return ReportData(
            range = range,
            summary = summary,
            points = points,
            transactions = filtered,
        )
    }

    private fun buildSummary(items: List<Sale>): ReportSummary {
        val total = items.sumOf { it.netTotal }
        val transactions = items.size
        val avg = if (transactions == 0) 0.0 else total / transactions

        val topByVolume = items
            .groupBy { it.productName }
            .mapValues { it.value.sumOf { sale -> sale.quantity } }
            .maxByOrNull { it.value }
            ?.key
            ?: "Sin ventas"

        val topByValue = items
            .groupBy { it.productName }
            .mapValues { it.value.sumOf { sale -> sale.netTotal } }
            .maxByOrNull { it.value }
            ?.key
            ?: "Sin ventas"

        val byCategory = items
            .groupBy { it.category }
            .map { (category, sales) ->
                CategoryAmount(category = category, amount = sales.sumOf { it.netTotal })
            }
            .sortedByDescending { it.amount }

        return ReportSummary(
            totalAmount = total,
            transactionCount = transactions,
            averageTicket = avg,
            topProductByVolume = topByVolume,
            topProductByValue = topByValue,
            categoryBreakdown = byCategory,
        )
    }

    private fun buildPoints(items: List<Sale>, period: ReportPeriod): List<ReportPoint> {
        val grouped = items.groupBy { sale ->
            when (period) {
                ReportPeriod.Daily -> dayBucket(sale.createdAtMillis)
                ReportPeriod.Weekly -> weekBucket(sale.createdAtMillis)
                ReportPeriod.Monthly -> monthBucket(sale.createdAtMillis)
                ReportPeriod.Annual -> yearBucket(sale.createdAtMillis)
                ReportPeriod.Custom -> dayBucket(sale.createdAtMillis)
            }
        }

        return grouped
            .toSortedMap(compareByDescending { it })
            .map { (bucket, sales) ->
                ReportPoint(
                    label = bucket,
                    totalAmount = sales.sumOf { it.netTotal },
                    transactionCount = sales.size,
                )
            }
    }

    private fun dayBucket(millis: Long): String = "D-${millis / MILLIS_PER_DAY}"

    private fun weekBucket(millis: Long): String = "W-${millis / MILLIS_PER_WEEK}"

    private fun monthBucket(millis: Long): String = "M-${millis / MILLIS_PER_MONTH}"

    private fun yearBucket(millis: Long): String = "Y-${millis / MILLIS_PER_YEAR}"

    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    private const val MILLIS_PER_WEEK = 7L * MILLIS_PER_DAY
    private const val MILLIS_PER_MONTH = 30L * MILLIS_PER_DAY
    private const val MILLIS_PER_YEAR = 365L * MILLIS_PER_DAY
}


