package org.salestrack.app.domain.usecase.print

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.mock.MockSalesFactory
import org.salestrack.app.data.repository.FakePrintRepository
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import kotlin.test.Test
import kotlin.test.assertTrue

class PrintReportUseCaseTest {

    @Test
    fun should_print_when_report_has_data() = runTest {
        val timeProvider = FakeTimeProvider(10_000L)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(MockSalesFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )
        val useCase = PrintReportUseCase(saleRepository, FakePrintRepository())

        val result = useCase()

        assertTrue(result is AppResult.Success)
    }
}
