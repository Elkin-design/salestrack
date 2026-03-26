package org.salestrack.app.domain.usecase.reports

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.model.Sale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCustomRangeReportUseCaseTest {

    @Test
    fun should_return_failure_for_inverted_range() = runTest {
        val repository = FakeSaleRepository(InMemorySaleDataSource(emptyList()), FakeTimeProvider(1_000L))
        val useCase = GetCustomRangeReportUseCase(repository)

        val result = useCase(fromMillis = 10_000L, toMillis = 1_000L)

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun should_filter_transactions_by_category() = runTest {
        val now = 100_000L
        val repository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(
                listOf(
                    Sale("1", "Cafe", "Bebidas", 1, 5_000.0, 0.0, now, "Ana"),
                    Sale("2", "Papas", "Snacks", 1, 4_000.0, 0.0, now, "Luis"),
                ),
            ),
            timeProvider = FakeTimeProvider(now),
        )
        val useCase = GetCustomRangeReportUseCase(repository)

        val result = useCase(
            fromMillis = now - 1_000L,
            toMillis = now + 1_000L,
            category = "Bebidas",
        )

        assertTrue(result is AppResult.Success)
        assertEquals(1, result.value.transactions.size)
        assertEquals("Cafe", result.value.transactions.first().productName)
    }
}

