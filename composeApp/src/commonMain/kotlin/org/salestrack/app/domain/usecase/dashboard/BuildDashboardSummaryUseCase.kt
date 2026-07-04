package org.salestrack.app.domain.usecase.dashboard

import org.salestrack.app.domain.model.DashboardSummary
import org.salestrack.app.domain.model.Sale

class BuildDashboardSummaryUseCase {
    operator fun invoke(todaySales: List<Sale>): DashboardSummary {
        val validTodaySales = todaySales.filter { !it.isDeleted }

        val topProduct = validTodaySales
            .groupBy { it.productName }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .maxByOrNull { it.value }
            ?.key
            ?: "Sin ventas"

        return DashboardSummary(
            totalSoldToday = validTodaySales.sumOf { it.netTotal },
            transactionCountToday = validTodaySales.size,
            topProductToday = topProduct,
            syncStatus = "Sincronizado",
        )
    }
}

