package org.salestrack.app.domain.usecase.export

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.export.BasicPdfExportAdapter
import org.salestrack.app.data.export.SpreadsheetXmlExcelExportAdapter
import org.salestrack.app.data.mock.MockSalesFactory
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.repository.RealExportRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.model.ExportDestination
import kotlin.test.Test
import kotlin.test.assertTrue

class ExportExcelUseCaseTest {

    @Test
    fun should_generate_excel_with_summary_and_detail() = runTest {
        val timeProvider = FakeTimeProvider(10_000L)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(MockSalesFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )
        val useCase = ExportExcelUseCase(
            saleRepository = saleRepository,
            exportRepository = RealExportRepository(
                pdfAdapter = BasicPdfExportAdapter(),
                excelAdapter = SpreadsheetXmlExcelExportAdapter(),
            ),
        )

        val result = useCase(destination = ExportDestination.SaveLocal)

        assertTrue(result is AppResult.Success)
        assertTrue(result.value.fileName.endsWith(".xls"))
        assertTrue(result.value.preview.contains("Resumen+Detalle"))
    }

    @Test
    fun should_include_seller_column_when_team_report_is_enabled() = runTest {
        val timeProvider = FakeTimeProvider(10_000L)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(MockSalesFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )
        val useCase = ExportExcelUseCase(
            saleRepository = saleRepository,
            exportRepository = RealExportRepository(
                pdfAdapter = BasicPdfExportAdapter(),
                excelAdapter = SpreadsheetXmlExcelExportAdapter(),
            ),
        )

        val result = useCase(
            destination = ExportDestination.SaveLocal,
            includeSellerColumn = true,
        )

        assertTrue(result is AppResult.Success)
        assertTrue(result.value.preview.contains("Vendedor"))
    }
}
