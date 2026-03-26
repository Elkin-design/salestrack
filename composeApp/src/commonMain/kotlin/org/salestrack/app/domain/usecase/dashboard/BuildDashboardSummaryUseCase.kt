package org.salestrack.app.domain.usecase.dashboard

import org.salestrack.app.domain.model.DashboardSummary
import org.salestrack.app.domain.model.Sale

class BuildDashboardSummaryUseCase {
    operator fun invoke(sales: List<Sale>, nowMillis: Long): DashboardSummary {
        val todayKey = dayKey(nowMillis)
        val todaySales = sales.filter { dayKey(it.createdAtMillis) == todayKey && !it.isDeleted }

        val topProduct = todaySales
            .groupBy { it.productName }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .maxByOrNull { it.value }
            ?.key
            ?: "Sin ventas"

        return DashboardSummary(
            totalSoldToday = todaySales.sumOf { it.netTotal },
            transactionCountToday = todaySales.size,
            topProductToday = topProduct,
            syncStatus = "Sincronizado",
        )
    }

    private fun dayKey(millis: Long): Long = millis / MILLIS_PER_DAY

    private companion object {
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }
}

