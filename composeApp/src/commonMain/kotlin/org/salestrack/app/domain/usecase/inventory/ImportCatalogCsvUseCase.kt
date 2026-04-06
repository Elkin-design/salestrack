package org.salestrack.app.domain.usecase.inventory

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.CatalogImportResult
import org.salestrack.app.domain.repository.InventoryRepository

class ImportCatalogCsvUseCase(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(csvContent: String): AppResult<CatalogImportResult> {
        if (csvContent.isBlank()) {
            return AppResult.Failure(IllegalArgumentException("El contenido CSV no puede estar vacio"))
        }
        return repository.importCatalogCsv(csvContent)
    }
}
