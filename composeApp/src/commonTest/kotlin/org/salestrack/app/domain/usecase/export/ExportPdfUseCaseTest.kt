package org.salestrack.app.domain.usecase.export

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.mock.MockSalesFactory
import org.salestrack.app.data.repository.FakeExportRepository
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.model.ExportDestination
import kotlin.test.Test
import kotlin.test.assertTrue

class ExportPdfUseCaseTest {

    @Test
    fun should_export_pdf_successfully() = runTest {
        val timeProvider = FakeTimeProvider(10_000L)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(MockSalesFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )
        val useCase = ExportPdfUseCase(saleRepository, FakeExportRepository())

        val result = useCase(ExportDestination.SaveLocal)

        assertTrue(result is AppResult.Success)
        assertTrue(result.value.fileName.endsWith(".pdf"))
    }
}
