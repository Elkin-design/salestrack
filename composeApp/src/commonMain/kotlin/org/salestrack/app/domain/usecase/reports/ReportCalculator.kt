package org.salestrack.app.domain.usecase.reports

import org.salestrack.app.domain.model.CategoryAmount
import org.salestrack.app.domain.model.ReportData
import org.salestrack.app.domain.model.ReportPeriod
import org.salestrack.app.domain.model.ReportPoint
import org.salestrack.app.domain.model.ReportRange
import org.salestrack.app.domain.model.ReportSummary
import org.salestrack.app.domain.model.Sale
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
        val points = buildPoints(filtered, period, range)

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

    private fun buildPoints(items: List<Sale>, period: ReportPeriod, range: ReportRange): List<ReportPoint> {
        val timeZone = TimeZone.currentSystemDefault()
        val startDateTime = Instant.fromEpochMilliseconds(range.fromMillis).toLocalDateTime(timeZone)
        val endDateTime = Instant.fromEpochMilliseconds(range.toMillis).toLocalDateTime(timeZone)

        // Generate buckets based on period
        val buckets = mutableListOf<String>()
        val labels = mutableListOf<String>()

        when (period) {
            ReportPeriod.Daily -> {
                for (hour in 0..23) {
                    buckets.add(String.format("%02d", hour))
                    labels.add(String.format("%02d:00", hour))
                }
            }
            ReportPeriod.Weekly -> {
                val dayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                for (i in 0..6) {
                    val targetMillis = range.fromMillis + (i * 24L * 60L * 60L * 1000L)
                    val dt = Instant.fromEpochMilliseconds(targetMillis).toLocalDateTime(timeZone)
                    buckets.add("${dt.year}-${dt.monthNumber}-${dt.dayOfMonth}")
                    labels.add(dayNames[dt.dayOfWeek.ordinal])
                }
            }
            ReportPeriod.Monthly -> {
                val endDay = endDateTime.dayOfMonth
                for (i in 1..endDay) {
                    buckets.add("${startDateTime.year}-${startDateTime.monthNumber}-$i")
                    labels.add(i.toString())
                }
            }
            ReportPeriod.Annual -> {
                val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                for (i in 1..12) {
                    buckets.add("${startDateTime.year}-$i")
                    labels.add(monthNames[i - 1])
                }
            }
            ReportPeriod.Custom -> {
                // If diff is <= 31 days, daily, else monthly
                val diffDays = (range.toMillis - range.fromMillis) / (24L * 60L * 60L * 1000L)
                if (diffDays <= 31) {
                    for (i in 0..diffDays) {
                        val targetMillis = range.fromMillis + (i * 24L * 60L * 60L * 1000L)
                        val dt = Instant.fromEpochMilliseconds(targetMillis).toLocalDateTime(timeZone)
                        buckets.add("${dt.year}-${dt.monthNumber}-${dt.dayOfMonth}")
                        labels.add(dt.dayOfMonth.toString())
                    }
                } else {
                    for (i in 0..(diffDays/30)) {
                        val targetMillis = range.fromMillis + (i * 30L * 24L * 60L * 60L * 1000L)
                        val dt = Instant.fromEpochMilliseconds(targetMillis).toLocalDateTime(timeZone)
                        val b = "${dt.year}-${dt.monthNumber}"
                        if (!buckets.contains(b)) {
                            buckets.add(b)
                            val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
                            labels.add(monthNames[dt.monthNumber - 1])
                        }
                    }
                }
            }
        }

        // Group items
        val grouped = items.groupBy { sale ->
            val dt = Instant.fromEpochMilliseconds(sale.createdAtMillis).toLocalDateTime(timeZone)
            when (period) {
                ReportPeriod.Daily -> String.format("%02d", dt.hour)
                ReportPeriod.Weekly, ReportPeriod.Monthly -> "${dt.year}-${dt.monthNumber}-${dt.dayOfMonth}"
                ReportPeriod.Annual -> "${dt.year}-${dt.monthNumber}"
                ReportPeriod.Custom -> {
                    val diffDays = (range.toMillis - range.fromMillis) / (24L * 60L * 60L * 1000L)
                    if (diffDays <= 31) "${dt.year}-${dt.monthNumber}-${dt.dayOfMonth}"
                    else "${dt.year}-${dt.monthNumber}"
                }
            }
        }

        return buckets.mapIndexed { index, bucket ->
            val bucketSales = grouped[bucket] ?: emptyList()
            ReportPoint(
                label = labels[index],
                totalAmount = bucketSales.sumOf { it.netTotal },
                transactionCount = bucketSales.size,
            )
        }
    }
}


