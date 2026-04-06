package org.salestrack.app.domain.usecase.inventory

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.CatalogExportFile
import org.salestrack.app.domain.repository.InventoryRepository

class ExportCatalogCsvUseCase(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(): AppResult<CatalogExportFile> = repository.exportCatalogCsv()
}
