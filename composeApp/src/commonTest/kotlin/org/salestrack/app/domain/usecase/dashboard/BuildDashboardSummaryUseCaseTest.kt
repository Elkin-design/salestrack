package org.salestrack.app.domain.usecase.dashboard

import org.salestrack.app.domain.model.Sale
import kotlin.test.Test
import kotlin.test.assertEquals

class BuildDashboardSummaryUseCaseTest {

    private val useCase = BuildDashboardSummaryUseCase()

    @Test
    fun should_calculate_today_summary_correctly() {
        val now = 3_000_000L
        val sales = listOf(
            Sale(
                id = "1",
                productName = "Cafe",
                category = "Bebidas",
                quantity = 2,
                unitPrice = 4_000.0,
                discount = 1_000.0,
                createdAtMillis = now,
                sellerName = "Ana",
            ),
            Sale(
                id = "2",
                productName = "Cafe",
                category = "Bebidas",
                quantity = 1,
                unitPrice = 4_000.0,
                discount = 0.0,
                createdAtMillis = now,
                sellerName = "Luis",
            ),
            Sale(
                id = "3",
                productName = "Galletas",
                category = "Snacks",
                quantity = 1,
                unitPrice = 2_000.0,
                discount = 0.0,
                createdAtMillis = now - 90_000_000L,
                sellerName = "Ana",
            ),
        )

        val summary = useCase(sales = sales, nowMillis = now)

        assertEquals(2, summary.transactionCountToday)
        assertEquals(11_000.0, summary.totalSoldToday)
        assertEquals("Cafe", summary.topProductToday)
    }
}

