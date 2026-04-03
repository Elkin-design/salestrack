package org.salestrack.app.data.repository

import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.BackupArtifact
import org.salestrack.app.domain.model.BackupPayload
import org.salestrack.app.domain.repository.BackupRepository

class FakeBackupRepository : BackupRepository {
    override suspend fun createBackup(payload: BackupPayload): AppResult<BackupArtifact> {
        val fileName = "salestrack_backup.json"
        val jsonPreview = "sales=${payload.salesCount}, products=${payload.productsCount}, categories=${payload.categoriesCount}"
        val excelPreview = "Resumen,Detalle"
        return AppResult.Success(
            BackupArtifact(
                fileName = fileName,
                jsonPreview = jsonPreview,
                excelPreview = excelPreview,
            ),
        )
    }
}
