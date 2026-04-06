package org.salestrack.app.domain.usecase.inventory

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.mock.MockInventoryFactory
import org.salestrack.app.data.repository.FakeInventoryRepository
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportCatalogCsvUseCaseTest {

    @Test
    fun should_import_valid_csv_rows() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = ImportCatalogCsvUseCase(repository)

        val result = useCase(
            "name,description,unitPrice,unit,barcode,category,stock,minimumStock\nAvena,Bolsa,6000,Bolsa,770111,Granos,10,3",
        )

        assertTrue(result is AppResult.Success)
        assertEquals(1, result.value.importedRows)
    }

    @Test
    fun should_fail_when_csv_is_blank() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = ImportCatalogCsvUseCase(repository)

        val result = useCase("")

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun should_report_error_for_invalid_row() = runTest {
        val repository = FakeInventoryRepository(
            dataSource = InMemoryInventoryDataSource(MockInventoryFactory.create()),
            timeProvider = FakeTimeProvider(1_000L),
        )
        val useCase = ImportCatalogCsvUseCase(repository)

        val result = useCase(
            "name,description,unitPrice,unit,barcode,category,stock,minimumStock\nSinPrecio,Item,,Unidad,,General,2,1",
        )

        assertTrue(result is AppResult.Success)
        assertEquals(1, result.value.failedRows)
    }
}
