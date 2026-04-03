package org.salestrack.app.presentation.feature.export

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeDispatcherProvider
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.export.BasicPdfExportAdapter
import org.salestrack.app.data.export.SpreadsheetXmlExcelExportAdapter
import org.salestrack.app.data.mock.MockSalesFactory
import org.salestrack.app.data.repository.FakeSaleRepository
import org.salestrack.app.data.repository.RealExportRepository
import org.salestrack.app.data.source.InMemorySaleDataSource
import org.salestrack.app.domain.model.ExportFormat
import org.salestrack.app.domain.usecase.export.ExportCsvUseCase
import org.salestrack.app.domain.usecase.export.ExportExcelUseCase
import org.salestrack.app.domain.usecase.export.ExportPdfUseCase
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExportReportViewModelTest {

    @Test
    fun should_export_using_selected_format() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(10_000L)
        val saleRepository = FakeSaleRepository(
            dataSource = InMemorySaleDataSource(MockSalesFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )
        val exportRepository = RealExportRepository(
            pdfAdapter = BasicPdfExportAdapter(),
            excelAdapter = SpreadsheetXmlExcelExportAdapter(),
        )

        val viewModel = ExportReportViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            exportPdfUseCase = ExportPdfUseCase(saleRepository, exportRepository),
            exportExcelUseCase = ExportExcelUseCase(saleRepository, exportRepository),
            exportCsvUseCase = ExportCsvUseCase(saleRepository, exportRepository),
        )

        viewModel.onEvent(ExportReportUiEvent.FormatChanged(ExportFormat.Excel))
        viewModel.onEvent(ExportReportUiEvent.ExportClicked)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.lastResult != null)
        assertTrue(viewModel.state.value.errorMessage == null)
    }
}
